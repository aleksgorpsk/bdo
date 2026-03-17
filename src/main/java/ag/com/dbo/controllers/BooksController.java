package ag.com.dbo.controllers;

import static org.springframework.http.HttpHeaders.LOCATION;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import ag.com.dbo.services.HttpBinService;
import ag.com.dbo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import ag.com.dbo.models.BookDTO;
import ag.com.dbo.services.BooksService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "Books")
@Slf4j
@OpenAPIDefinition(info = @Info(title = "java.samples.spring.boot", version = "1.0", description = "🧪 Proof of Concept for a RESTful Web Service made with JDK 21 (LTS) and Spring Boot 3", contact = @Contact(name = "GitHub", url = "https://github.com/nanotaboada/java.samples.spring.boot"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")))
public class BooksController {

    private final BooksService booksService;
    private final HttpBinService httpBinService;
    private final JwtUtil jwtUtil;

    public BooksController(BooksService booksService, HttpBinService httpBinService, JwtUtil jwtUtil) {
        this.booksService = booksService;
        this.httpBinService = httpBinService;
        this.jwtUtil = jwtUtil;
    }

    /*
     * -------------------------------------------------------------------------
     * HTTP POST
     * -------------------------------------------------------------------------
     */

    @PostMapping("/books")
    @Operation(summary = "Creates a new book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public ResponseEntity<Void> post(@RequestBody @Valid BookDTO bookDTO) {
        boolean created = booksService.create(bookDTO);
        if (!created) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        URI location = MvcUriComponentsBuilder
                .fromMethodCall(MvcUriComponentsBuilder.on(BooksController.class).getByIsbn(bookDTO.getIsbn()))
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(LOCATION, location.toString())
                .build();
    }

    /*
     * -------------------------------------------------------------------------
     * HTTP GET
     * -------------------------------------------------------------------------
     */

    @GetMapping("/books/{isbn}")
    @Operation(summary = "Retrieves a book by its ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content)
    })
    public ResponseEntity<BookDTO> getByIsbn(@PathVariable String isbn) {
        BookDTO bookDTO = booksService.retrieveByIsbn(isbn);
        return (bookDTO != null)
                ? ResponseEntity.status(HttpStatus.OK).body(bookDTO)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/books")
    @Operation(summary = "Retrieves all books")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO[].class)))
    })
    public ResponseEntity<List<BookDTO>> getAll(
            @RequestHeader("Authorization")  String authHeader
    ) {
     //   String ops = httpBinService.getJson();
//        log.info("!!!!"+ops);
        log.info("!!!! getAll"+ authHeader);
  //      String usName = jwtUtil.extractUsername(authHeader.substring(7));
    //    log.info("!!!! usName"+ usName);

        try {
            List<BookDTO> books = booksService.retrieveAll();
            return ResponseEntity.status(HttpStatus.OK).body(books);
        } catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }

    @GetMapping("/books/search")
    @Operation(summary = "Searches books by description keyword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - Returns matching books (or empty array if none found)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO[].class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Missing or blank description parameter", content = @Content)
    })
    public ResponseEntity<List<BookDTO>> searchByDescription(
            @RequestParam @NotBlank(message = "Description parameter must not be blank") String description) {
        List<BookDTO> books = booksService.searchByDescription(description);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    /*
     * -------------------------------------------------------------------------
     * HTTP PUT
     * -------------------------------------------------------------------------
     */

    @PutMapping("/books")
    @Operation(summary = "Updates (entirely) a book by its ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content)
    })
    public ResponseEntity<Void> put(@RequestBody @Valid BookDTO bookDTO) {
        boolean updated = booksService.update(bookDTO);
        return (updated)
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /*
     * -------------------------------------------------------------------------
     * HTTP DELETE
     * -------------------------------------------------------------------------
     */

    @DeleteMapping("/books/{isbn}")
    @Operation(summary = "Deletes a book by its ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable @ISBN String isbn) {
        boolean deleted = booksService.delete(isbn);
        return (deleted)
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
