-- Insert default PACS configurations

-- Primary TEAMPACS server (default)
INSERT INTO application_entities (
    ae_title,
    hostname,
    port,
    ae_type,
    description,
    query_retrieve_level,
    is_default,
    is_enabled,
    connection_timeout,
    response_timeout,
    max_associations,
    created_at,
    updated_at
) VALUES (
    'TEAMPACS',
    '117.247.185.219',
    11112,
    'REMOTE_LEGACY',
    'Default TEAMPACS server',
    'STUDY',
    TRUE,
    TRUE,
    30000,
    60000,
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Local PACS server
INSERT INTO application_entities (
    ae_title,
    hostname,
    port,
    ae_type,
    description,
    query_retrieve_level,
    is_default,
    is_enabled,
    connection_timeout,
    response_timeout,
    max_associations,
    created_at,
    updated_at
) VALUES (
    'LOCALPACS',
    'localhost',
    4242,
    'LOCAL',
    'Local PACS server for testing',
    'STUDY',
    FALSE,
    TRUE,
    30000,
    60000,
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Orthanc PACS server
INSERT INTO application_entities (
    ae_title,
    hostname,
    port,
    ae_type,
    description,
    dicomweb_url,
    query_retrieve_level,
    is_default,
    is_enabled,
    connection_timeout,
    response_timeout,
    max_associations,
    created_at,
    updated_at
) VALUES (
    'ORTHANC',
    'localhost',
    4242,
    'REMOTE_DICOMWEB',
    'Orthanc DICOM Web server',
    'http://localhost:8042/dicom-web',
    'STUDY',
    FALSE,
    FALSE,
    30000,
    60000,
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- DCM4CHEE Archive
INSERT INTO application_entities (
    ae_title,
    hostname,
    port,
    ae_type,
    description,
    dicomweb_url,
    query_retrieve_level,
    is_default,
    is_enabled,
    connection_timeout,
    response_timeout,
    max_associations,
    created_at,
    updated_at
) VALUES (
    'DCM4CHEE',
    'localhost',
    11112,
    'REMOTE_LEGACY',
    'DCM4CHEE Archive server',
    'http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs',
    'STUDY',
    FALSE,
    FALSE,
    30000,
    60000,
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test PACS server
INSERT INTO application_entities (
    ae_title,
    hostname,
    port,
    ae_type,
    description,
    query_retrieve_level,
    is_default,
    is_enabled,
    connection_timeout,
    response_timeout,
    max_associations,
    created_at,
    updated_at
) VALUES (
    'TESTPACS',
    '127.0.0.1',
    11113,
    'REMOTE_LEGACY',
    'Test PACS server',
    'STUDY',
    FALSE,
    FALSE,
    30000,
    60000,
    5,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
