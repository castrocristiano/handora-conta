package com.zupedu.conta.conta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/contas")
public class ContaController {

    Logger logger = LoggerFactory.getLogger(ContaController.class);

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ContaNovaProducer contaNovaProducer;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> inserir(@Valid @RequestBody ContaRequest request){
        logger.info("Cadastrando uma nova Conta");
        var conta = request.toModel();

        if(contaRepository.findByDocumentoTitular(conta.getDocumentoTitular()).isPresent()){

            return ResponseEntity.badRequest().body("Já existe uma conta com mesmo CPF!");

        }else{
            conta = contaRepository.save(conta);

            logger.info("Conta cadastrada com numero: {} e agência: {} ", conta.getNumero(), conta.getAgencia());

            contaNovaProducer.criar(conta);

            logger.info("Evento de conta cadastrada publicado com numero: {} e agência: {} ", conta.getNumero(), conta.getAgencia());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ContaResponse.from(conta));
        }
    }


    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id){
        var conta = contaRepository.findById(id)
                .orElseThrow(ContaIdInexistenteException::new);

        contaRepository.delete(conta);

        logger.info("Conta excluída com sucesso de id {} ", id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultar(@PathVariable Long id){
        var conta = contaRepository.findById(id);

        if(conta.isEmpty()){
            return ResponseEntity.badRequest().body("Não foi possível encontrar conta com este id");
        }

        logger.info("Conta de id {} consultada com sucesso", id);

        return ResponseEntity.ok().body(ContaResponse.from(conta.get()));
    }
}
