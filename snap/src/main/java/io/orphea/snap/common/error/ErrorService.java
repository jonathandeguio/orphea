package io.orphea.snap.common.error;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ErrorService {
    private final ErrorRepository errorRepository;

    public List<ErrorModel> getAllErrors() {
        return errorRepository.findAll();
    }

    public ErrorModel save(ErrorDTO errorDTO) {
        return errorRepository.save(ErrorModel.builder().name(errorDTO.getName()).message(errorDTO.getMessage()).stack(errorDTO.getStack()).componentStack(errorDTO.getComponentStack()).build());
    }
}
