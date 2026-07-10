package io.movetodata.library.services;

import org.springframework.stereotype.Repository;


import io.movetodata.library.models.TemplateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository
        extends JpaRepository<TemplateModel, Long> {
}