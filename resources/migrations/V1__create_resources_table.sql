CREATE TABLE fhir_resources
(
    id            SERIAL PRIMARY KEY,
    resource_id   VARCHAR(255) NOT NULL,
    patient_id    VARCHAR(255) NOT NULL,
    resource_type VARCHAR(255) NOT NULL,
    content       JSONB        NOT NULL
);