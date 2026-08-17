package ag.com.dbo.services.management.impl;

import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.EtlInstanceDto;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.services.management.EtlInstanceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtlInstanceServiceImpl implements EtlInstanceService {

    private final EtlInstanceRepository etlInstanceRepository;
    private final ModelMapper modelMapper;

    /**
     * Create EtlInstanceDto
     * @param etlInstanceDTO
     * @return
     */
    public EtlInstanceDto create(EtlInstanceDto etlInstanceDTO) {
            EtlInstance etl = mapFrom(etlInstanceDTO);
            EtlInstance newEtl =etlInstanceRepository.saveAndFlush(etl);
            return mapFrom(newEtl);
    }

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    public EtlInstanceDto retrieveById(BigInteger id) {
        return etlInstanceRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }



    public Page<@NonNull EtlInstanceDto> retrievePage(BigInteger etlId, PageRequest pageable){
        Page<@NonNull EtlInstance> entities = etlInstanceRepository.findByEtl(etlId, pageable);
        return entities.map(e-> modelMapper.map(e, EtlInstanceDto.class));

    }

    public Page<@NonNull EtlInstanceDto> retrievePage(BigInteger etlId, Pageable pageable ){
        Page<@NonNull EtlInstance> entities = etlInstanceRepository.findByEtl(etlId, pageable);
        return entities.map(e-> modelMapper.map(e, EtlInstanceDto.class));

    }

    public Page<@NonNull EtlInstanceDto> findByEtlContainingIgnoreCase(BigInteger etlId, String keyword, Pageable pageable){
        Page<@NonNull EtlInstance> etlPage = etlInstanceRepository.findByNameContainingIgnoreCase( keyword, etlId, pageable);
        return convert(etlPage);

    }
    public Optional<EtlInstanceDto> findById(BigInteger id){
        Optional<EtlInstance> e=etlInstanceRepository.findById(id);
        if (e.isPresent()) {
            return Optional.of(mapFrom(e.get()));
        }
        return Optional.empty();
    }

    public List<EtlInstanceDto> findByStatus(BigInteger etlId, String status){
        return etlInstanceRepository.findByEtlIdAndStatus(etlId, status).stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl by status:"+ x.toString()))
                .toList();

    }

    public List<EtlInstanceDto> retrieveAll() {
        return etlInstanceRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    public List<EtlInstance> retrieveAll0() {
        return etlInstanceRepository.findAll();
    }

    public List<EtlInstanceDto> retrievePage() {

        log.info("retrievePage");
        return StreamSupport.stream(etlInstanceRepository.findAll().spliterator(), false)
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }



    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

//    @CachePut(value = "etlInstances", key = "#etlInstanceDTO.etlInstanceId")
    public boolean update(EtlInstanceDto etlDTO) {
        if (etlInstanceRepository.existsById(etlDTO.getEtlInstanceId())) {
            etlInstanceRepository.save(mapFrom(etlDTO));
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

//    @CacheEvict(value = "etlInstances", key = "#etlInstanceId")
    public boolean delete(BigInteger id) {
        if (etlInstanceRepository.existsById(id)) {
            etlInstanceRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<EtlInstanceDto> convert(Page<EtlInstance> etlp){
        Page<EtlInstanceDto> dtoPage = etlp.map(new Function<EtlInstance, EtlInstanceDto>() {
            @Override
            public EtlInstanceDto apply(EtlInstance entity) {
                return mapFrom(entity);
            }
        });
        return dtoPage;

    }


    public EtlInstanceDto mapFrom(EtlInstance etl) {
        return modelMapper.map(etl, EtlInstanceDto.class);
    }

    public EtlInstance mapFrom(EtlInstanceDto dto) {
        return modelMapper.map(dto, EtlInstance.class);
    }

}
