package br.com.casadoamor.sgca.controller;

import br.com.casadoamor.sgca.dto.PessoaFisicaDto;
import br.com.casadoamor.sgca.service.PessoaFisicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/1.0", produces = "application/json;charset=UTF-8")
public class PessoaFisicaController {

    @Autowired
    private PessoaFisicaService pessoaFisicaService;

    @PostMapping(path="/pessoa-fisica")
    public ResponseEntity<PessoaFisicaDto> createPessoaFisica(@RequestBody PessoaFisicaDto pessoaFisicaDto) {
        return new ResponseEntity<>(this.pessoaFisicaService.createPessoaFisica(pessoaFisicaDto), HttpStatus.CREATED);
    }

    @GetMapping(path="/pessoa-fisica/{id}")
    public ResponseEntity<PessoaFisicaDto> getPessoaFisicaById(@PathVariable Long id) {
        return new ResponseEntity<>(this.pessoaFisicaService.getPessoaFisicaById(id), HttpStatus.OK);
    }

    @GetMapping(path="/pessoa-fisica/{nome}/nome")
    public ResponseEntity<List<PessoaFisicaDto>> getPessoaFisicaById(@PathVariable String nome) {
        return new ResponseEntity<>(this.pessoaFisicaService.findByNomeContaining(nome), HttpStatus.OK);
    }

    @GetMapping(path="/pessoa-fisica/{cpf:[0-9.-]+}/cpf")
    public ResponseEntity<PessoaFisicaDto> getPessoaFisicaByCpf(@PathVariable String cpf) {
        return new ResponseEntity<>(this.pessoaFisicaService.getPessoaFisicaByCpf(cpf), HttpStatus.OK);
    }

    @GetMapping(path = "/pessoa-fisica")
    public ResponseEntity<List<PessoaFisicaDto>> getAllPessoaFisica() {
        return new ResponseEntity<>(this.pessoaFisicaService.getAllPessoaFisica(), HttpStatus.OK);
    }

    @PutMapping(path="/pessoa-fisica/{id}")
    public ResponseEntity<PessoaFisicaDto> updatePessoaFisica(
            @PathVariable Long id,
            @RequestBody PessoaFisicaDto pessoaFisicaDto
    ){
        return new ResponseEntity<>(this.pessoaFisicaService.updatePessoaFisica(id, pessoaFisicaDto), HttpStatus.OK);
    }

    @DeleteMapping(path="/pessoa-fisica/{id}")
    public ResponseEntity<PessoaFisicaDto> deletePessoaFisica(@PathVariable Long id) {
        this.pessoaFisicaService.deletePessoaFisica(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
