CREATE TABLE IF NOT EXISTS ente (
    codice_ente VARCHAR(50) PRIMARY KEY,
    denominazione VARCHAR(255) NOT NULL,
    contatto VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS posizione (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iuv VARCHAR(35) NOT NULL UNIQUE,
    codice_fiscale VARCHAR(16) NOT NULL,
    tipo_tributo VARCHAR(50) NOT NULL,
    importo DECIMAL(10,2) NOT NULL,
    importo_originale DECIMAL(10,2) NOT NULL,
    stato VARCHAR(20) NOT NULL,
    data_emissione DATE NOT NULL,
    data_scadenza DATE NOT NULL,
    data_pagamento DATE NULL,
    codice_ente VARCHAR(50) NOT NULL,/*1 a n con tabella 'ente'*/
    FOREIGN KEY (codice_ente) REFERENCES ente(codice_ente)
);

CREATE TABLE IF NOT EXISTS operatore (
    codice_fiscale VARCHAR(16) PRIMARY KEY,
    codice_sede VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS archivio_pagamenti (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iuv VARCHAR(35) NOT NULL UNIQUE,
    id_transazione VARCHAR(35) NOT NULL UNIQUE,
    data_pagamento DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS notifica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iuv VARCHAR(35) NOT NULL, /*1 a n con tabella 'posizione'*/
    data_invio DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL, /*EMISSIONE,SOLLECITO15,SOLLECITO30*/
    FOREIGN KEY (iuv) REFERENCES posizione(iuv),
    CONSTRAINT uq_notifica_iuv_tipo UNIQUE (iuv, tipo) /* in modo da avere una sola istanza di coppie*/
);

INSERT INTO operatore (codice_fiscale, codice_sede) VALUES
    ('VRDLGI75B02F205Y', 'COMUNE_ROMA');

-- Dati di test
INSERT INTO ente (codice_ente, denominazione, contatto) VALUES
    ('COMUNE_ROMA', 'Comune di Roma', '06-12345678'),
    ('COMUNE_MILANO', 'Comune di Milano', '02-87654321'),
    ('POLIZIA_DI_STATO', 'Polizia di Stato', '02-8765666'),
    ('AMAZON_LTD', 'Amazon', '02-8765666');

INSERT INTO posizione (iuv, codice_fiscale, tipo_tributo, importo, importo_originale, stato, data_emissione, data_scadenza, codice_ente) VALUES
    ('12345678901234567', 'RSSMRA80A01H501X', 'TARI', 250.00, 250.00, 'DA_PAGARE', '2026-11-01', '2026-12-01', 'COMUNE_ROMA'),
    ('12345678901234568', 'RSSMRA80A01H501X', 'IMU', 800.00, 800.00, 'DA_PAGARE', '2026-11-02', '2026-12-02', 'COMUNE_ROMA');