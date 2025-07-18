package com.example.backend.infrastructure.adapter.in.web;

import com.example.backend.application.usecase.cadastro.*;
import com.example.backend.domain.model.PessoaCadastro;
import com.example.backend.infrastructure.adapter.in.web.exception.ResourceNotFoundException;
import com.example.backend.infrastructure.dto.PessoaRequest;
import com.example.backend.infrastructure.dto.PessoaCadastroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pessoa")
@RequiredArgsConstructor
@Slf4j
@Tag(name= "Backend", description = "Gerenciar informações de Pessoas")
public class PessoaCadastroController {

    private final CreatePessoaCadastroUseCase createPessoaUseCase;
    private final GetAllPessoasCadastroUseCase getAllPessoasCadastroUseCase;
    private final GetPessoaCadastroUseCase getPessoaUseCase;
    private final UpdatePessoaCadastroUseCase updatePessoaUseCase;
    private final DeletePessoaCadastroUseCase deletePessoaUseCase;

    /**
     * Trata a request para gravar uma nova pessoa.
     * Esse endpoit deve processar a PessoaRequest request e com repository persistir para o banco de dados.
     */
    @Operation(summary = "Criar uma nova pessoa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso!"),
            @ApiResponse(responseCode = "400", description = "Solicitação inválida!"),
            @ApiResponse(responseCode = "409", description = "Conflito! A pessoa já existe"),
            @ApiResponse(responseCode = "422", description = "Erro! A criar pessoa possui dados inválidos"),
    })
    @PostMapping
    public ResponseEntity<PessoaCadastroResponse> createPessoa(@Valid @RequestBody PessoaRequest request) {
        log.info("Foi recebida a request para criar a pessoa: {}", request.getNome());
        var pessoa = createPessoaUseCase.execute(request.toDomain());
        return new ResponseEntity<>(PessoaCadastroResponse.fromDomain(pessoa), HttpStatus.CREATED);
    }

    /**
     * Obtém a Pessoa pelo ID.
     */
    @Operation(summary = "Obter uma pessoa por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PessoaCadastroResponse> getPessoa(@PathVariable Integer id) {
        log.info("Foi reciba uma nova request para obter a Pessoa de ID: {}", id);
        var pessoa = getPessoaUseCase.execute(id)
                .orElseThrow(() -> {
                    log.warn("Pessoa com ID {} não encontrado", id);
                    return new ResourceNotFoundException("Pessoa não encontrada com ID: " + id);
                });
        return ResponseEntity.ok(PessoaCadastroResponse.fromDomain(pessoa));
    }

    /**
     * Obter todas as pessoas com paginação.
     */
    @Operation(summary = "Obter todas pessoas com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucesso ao obter a lista de Pessoas!")
    })
    @GetMapping
    public ResponseEntity<List<PessoaCadastroResponse>> getAllPessoas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Foi recibda a requisição para obter todas as Pessoas (page: {}, size: {})", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PessoaCadastro> pessoaPage = getAllPessoasCadastroUseCase.execute(pageable);
        List<PessoaCadastroResponse> pessoaCadastroRespons = pessoaPage.getContent().stream()
                .map(PessoaCadastroResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pessoaCadastroRespons);

    }

    /**
     * Atualiza uma pessoa já existente.
     */
    @Operation(summary = "Atualiza uma pessoa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PessoaCadastroResponse> updatePessoa(@PathVariable Integer id, @Valid @RequestBody PessoaRequest request) {
        log.info("Foi recebida a requisição para atualizar Pessoa com ID: {}", id);
        getPessoaUseCase.execute(id)
                .orElseThrow(() -> {
                    log.warn("Pessoa com ID {} não encontrada para realizar atualização", id);
                    return new ResourceNotFoundException("Pessoa não encontrada com id: " + id);
                });

        PessoaCadastro pessoaCadastroAtualizada = updatePessoaUseCase.execute(id, request.toDomain());

        return ResponseEntity.ok(PessoaCadastroResponse.fromDomain(pessoaCadastroAtualizada));
    }

    /**
     * Remove uma pessoa por ID.
     */
    @Operation(summary = "Remover uma pessoa por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pessoa removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePessoa(@PathVariable Integer id) {
        log.info("Foi recebida a requisição para remover Pessoa com ID: {}", id);
        getPessoaUseCase.execute(id)
                .orElseThrow(() -> {
                    log.warn("A Pessoa de ID {} não foi encontrada para realizar a remoção", id);
                    return new ResourceNotFoundException("Ṕessoa não encontrada com ID: " + id);
                });

        deletePessoaUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
