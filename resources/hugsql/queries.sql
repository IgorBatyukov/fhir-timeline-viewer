-- :name get-fhir-timeline :? :*
SELECT
    id,
    resource_type,
    jsonb_path_query_array(content, '$.category[*].text') AS categories,
    content ->> 'status' AS resource_status,
    content ->> 'effectiveDateTime' AS effective_dt
FROM fhir_resources
WHERE 1=1
/*~ (when-let [patient-id (:patient_id params)] */
    AND patient_id = :patient_id
/*~ ) ~*/
/*~ (when-let [resource-type (:resource_type params)] */
    AND resource_type = :resource_type
/*~ ) ~*/
ORDER BY content ->> 'effectiveDateTime' DESC

-- :name get-resource-by-id :? :1
SELECT
    id,
    resource_id,
    patient_id,
    resource_type,
    jsonb_path_query_array(content, '$.category[*].text') AS categories,
    content -> 'verificationStatus' ->> 'text'AS verification_status,
    content ->> 'status' AS resource_status,
    content -> 'code' ->> 'text' AS code_text,
    content ->> 'effectiveDateTime' AS effective_dt,
    content -> 'valueQuantity' AS value_quantity,
    content ->> 'onsetDateTime' AS onset_dt,
    content ->> 'recordedDate' AS recorded_dt
FROM fhir_resources
WHERE id = :id::integer