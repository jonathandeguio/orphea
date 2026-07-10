package io.movetodata.snap.sharedutils.DTO;

import io.movetodata.snap.sharedutils.models.PageDTO;
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