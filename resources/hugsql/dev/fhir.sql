-- :name seed-fhir-resources-table :insert :*
INSERT INTO fhir_resources (resource_type, resource_id, patient_id, content)
VALUES (:resource-type,
        :resource-id,
        :patient-id,
        :content::jsonb)
    RETURNING id;

-- :name drop-table!
-- :command :execute
DROP TABLE IF EXISTS :i:table-name;