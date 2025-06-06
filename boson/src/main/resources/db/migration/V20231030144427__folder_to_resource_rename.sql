alter table kitab_folder drop constraint FKbewi3hkg7l6db972534nj48gd;
alter table kitab_folder drop constraint FKlqub6ybebo8k1agxxx5v8ousm;
alter table kitab_folder drop constraint FK1xfy3oep9d4ooaqpwtg1m5wiv;

alter table kitab_folder drop constraint "kitab_folder_pkey";

ALTER TABLE kitab_folder RENAME TO kitab_resource;

-- Primary Key Constraint
ALTER TABLE public.kitab_resource
ADD CONSTRAINT kitab_resource_pkey PRIMARY KEY (id);

-- Foreign Key Constraints
ALTER TABLE public.kitab_resource
ADD CONSTRAINT fk1xfy3oep9d4ooaqpwtg1m5wiv FOREIGN KEY (updated_by_id)
    REFERENCES public.passport_users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE public.kitab_resource
ADD CONSTRAINT fkbewi3hkg7l6db972534nj48gd FOREIGN KEY (parent_id)
    REFERENCES public.kitab_resource (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE public.kitab_resource
ADD CONSTRAINT fklqub6ybebo8k1agxxx5v8ousm FOREIGN KEY (created_by_id)
    REFERENCES public.passport_users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;


