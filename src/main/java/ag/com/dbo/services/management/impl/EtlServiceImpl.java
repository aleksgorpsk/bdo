package ag.com.dbo.services.management.impl;

import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlDto;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.services.management.EtlService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtlServiceImpl implements EtlService {

    private final EtlRepository etlRepository;
    private final ModelMapper modelMapper;

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    @CachePut(value = "etls", key = "#etlDTO.id")
    public EtlDto create(EtlDto etlDTO) {
            Etl etl = mapFrom(etlDTO);
            Etl newEtl =etlRepository.saveAndFlush(etl);
            return mapFrom(newEtl);
    }

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    public EtlDto retrieveById(BigInteger id) {
        return etlRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }



    public Page<@NonNull EtlDto> retrievePage(PageRequest pageable){
        Page<@NonNull Etl> entities = etlRepository.findAll(pageable);
        return entities.map(e-> modelMapper.map(e, EtlDto.class));
    }

    public Page<@NonNull EtlDto> retrievePage(Pageable pageable){
        Page<@NonNull Etl> entities = etlRepository.findAll(pageable);
        return entities.map(e-> modelMapper.map(e, EtlDto.class));

    }

    public Page<@NonNull EtlDto> findByEtlContainingIgnoreCase(String keyword, Pageable pageable){
        Page<@NonNull Etl> etlPage = etlRepository.findByNameContainingIgnoreCase( keyword,  pageable);
        return convert(etlPage);

    }

    public Optional<EtlDto> findById(BigInteger id){
        Optional<Etl> e=etlRepository.findById(id);
        if (e.isPresent()) {
            return Optional.of(mapFrom(e.get()));
        }
        return Optional.empty();
    }

    public Optional<Etl> findEtlById(BigInteger id){
        return  etlRepository.findById(id);
    }

    public List<EtlDto> findByStatus(Integer status){
        return etlRepository.findByStatus(status).stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl by status:"+ x.toString()))
                .toList();

    }

    public List<EtlDto> retrieveAll() {
        return etlRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    public List<Etl> retrieveAll0() {
        return etlRepository.findAll();
    }

    public List<EtlDto> retrievePage() {

        log.info("retrievePage");
        return etlRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }



    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    public boolean update(EtlDto etlDTO) {
        if (etlRepository.existsById(etlDTO.getId())) {
            etlRepository.save(mapFrom(etlDTO));
            return true;
        } else {
            return false;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Delete
     * -------------------------------------------------------------------------
     */

    @CacheEvict(value = "etls", key = "#id")
    public boolean delete(BigInteger id) {
        if (etlRepository.existsById(id)) {
            etlRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<@NotNull EtlDto> convert(Page<@NotNull Etl> etlp){
        return etlp.map(new Function<Etl, EtlDto>() {
            @Override
            public EtlDto apply(Etl entity) {
                return mapFrom(entity);
            }
        });

    }


    public EtlDto mapFrom(Etl etl) {
        return modelMapper.map(etl, EtlDto.class);
    }

    public Etl mapFrom(EtlDto dto) {
        return modelMapper.map(dto, Etl.class);
    }

}
