package ag.com.dbo.services.management;

import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.EtlInstanceDTO;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtlInstanceService {

    private final EtlInstanceRepository etlInstanceRepository;
    private final ModelMapper modelMapper;

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    @CachePut(value = "etlInstances", key = "#etlInstanceDTO.etlInstanceId")
    public EtlInstanceDTO create(EtlInstanceDTO etlInstanceDTO) {
            EtlInstance etl = mapFrom(etlInstanceDTO);
            EtlInstance newEtl =etlInstanceRepository.saveAndFlush(etl);
            return mapFrom(newEtl);
    }

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    @Cacheable(value = "etlInstances", key = "#etlInstanceId")
    public EtlInstanceDTO retrieveById(BigInteger id) {
        return etlInstanceRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }



    public Page<@NonNull EtlInstanceDTO> retrievePage(BigInteger etlId,PageRequest pageable){
        Page<@NonNull EtlInstance> entities = etlInstanceRepository.findByEtl(etlId, pageable);
        return entities.map(e-> modelMapper.map(e, EtlInstanceDTO.class));

    }

    public Page<@NonNull EtlInstanceDTO> retrievePage(BigInteger etlId, Pageable pageable ){
        Page<@NonNull EtlInstance> entities = etlInstanceRepository.findByEtl(etlId, pageable);
        return entities.map(e-> modelMapper.map(e, EtlInstanceDTO.class));

    }

    public Page<@NonNull EtlInstanceDTO> findByEtlContainingIgnoreCase(BigInteger etlId, String keyword, Pageable pageable){
        Page<@NonNull EtlInstance> etlPage = etlInstanceRepository.findByNameContainingIgnoreCase( keyword, etlId, pageable);
        return convert(etlPage);

    }
    public Optional<EtlInstanceDTO> findById(BigInteger id){
        Optional<EtlInstance> e=etlInstanceRepository.findById(id);
        if (e.isPresent()) {
            return Optional.of(mapFrom(e.get()));
        }
        return Optional.empty();
    }

    public List<EtlInstanceDTO> findByStatus(BigInteger etlId, String status){
        return etlInstanceRepository.findByEtlIdAndStatus(etlId, status).stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl by status:"+ x.toString()))
                .toList();

    }

//    @Cacheable(value = "etlInstances")
    public List<EtlInstanceDTO> retrieveAll() {
        return etlInstanceRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    public List<EtlInstance> retrieveAll0() {
        return etlInstanceRepository.findAll();
    }

 //   @Cacheable(value = "etlInstances")
    public List<EtlInstanceDTO> retrievePage() {

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
    public boolean update(EtlInstanceDTO etlDTO) {
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

    public Page<EtlInstanceDTO> convert( Page<EtlInstance> etlp){
        Page<EtlInstanceDTO> dtoPage = etlp.map(new Function<EtlInstance, EtlInstanceDTO>() {
            @Override
            public EtlInstanceDTO apply(EtlInstance entity) {
                return mapFrom(entity);
            }
        });
        return dtoPage;

    }


    public EtlInstanceDTO mapFrom(EtlInstance etl) {
        return modelMapper.map(etl, EtlInstanceDTO.class);
    }

    public EtlInstance mapFrom(EtlInstanceDTO dto) {
        return modelMapper.map(dto, EtlInstance.class);
    }

}
