package io.movetodata.sharedutils.DTO;

import io.movetodata.sharedutils.models.PageDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageToPageDTOMapper<T> {

    public PageDTO<T> pageToPageDTO(Page<T> page) {
        PageDTO<T> pageDTO = new PageDTO<>();
        pageDTO.setContent(page.getContent());
        pageDTO.setTotalElements(page.getTotalElements());

        return pageDTO;
    }
}