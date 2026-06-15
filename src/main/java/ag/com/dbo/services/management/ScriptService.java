package ag.com.dbo.services.management;

import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptId;
import ag.com.dbo.repositories.management.ScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final ModelMapper modelMapper;

    public Optional<Script> retrieveById(ScriptId id) {
        return scriptRepository.findById(id);

    }
    public Optional<Script> getLastScript(String language, String name){
          List<Script> l = scriptRepository.getLastScript(language, name);
          if (CollectionUtils.isEmpty(l)){
              return Optional.empty();
          }else{
              return Optional.of(l.get(0));
          }

    }

}
