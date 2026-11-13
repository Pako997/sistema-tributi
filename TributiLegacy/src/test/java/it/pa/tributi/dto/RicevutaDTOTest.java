package it.pa.tributi.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RicevutaDTO")
class RicevutaDTOTest {

    private static final Date PAGAMENTO = new Date(1701000000000L);

    @Test
    @DisplayName("un DTO nuovo ha tutti i campi null")
    void campiInizialiNull() {
        RicevutaDTO dto = new RicevutaDTO();

        assertNull(dto.getCodiceFiscale());
        assertNull(dto.getTipoTributo());
        assertNull(dto.getIuv());
        assertNull(dto.getIdTransazione());
        assertNull(dto.getImporto());
        assertNull(dto.getCodiceEnte());
        assertNull(dto.getEnteEmittente());
        assertNull(dto.getContattoEnte());
        assertNull(dto.getDataPagamento());
    }

    @Test
    @DisplayName("ogni setter valorizza il proprio campo")
    void getterSetter() {
        RicevutaDTO dto = completo();

        assertEquals("RSSMRA80A01F839X", dto.getCodiceFiscale());
        assertEquals("MULTA", dto.getTipoTributo());
        assertEquals("IUV-1", dto.getIuv());
        assertEquals("TRX-001", dto.getIdTransazione());
        assertEquals(Double.valueOf(80.50), dto.getImporto());
        assertEquals("C_F839", dto.getCodiceEnte());
        assertEquals("Comune di Napoli", dto.getEnteEmittente());
        assertEquals("tributi@comune.napoli.it", dto.getContattoEnte());
        assertEquals(PAGAMENTO, dto.getDataPagamento());
    }

    @Test
    @DisplayName("l'importo è un Double: un BigDecimal convertito perde la precisione decimale")
    void importoDouble() {
        BigDecimal importoPosizione = new BigDecimal("100.10");
        RicevutaDTO dto = new RicevutaDTO();

        dto.setImporto(importoPosizione.doubleValue());

        assertNotEquals(importoPosizione, new BigDecimal(dto.getImporto()));
        assertEquals("100.099999999999994315658113919198513031005859375",
                new BigDecimal(dto.getImporto()).toPlainString());
    }

    @Test
    @DisplayName("la data è condivisa: modificare la Date originale modifica il DTO")
    void dataNonCopiata() {
        Date pagamento = new Date(PAGAMENTO.getTime());
        RicevutaDTO dto = new RicevutaDTO();
        dto.setDataPagamento(pagamento);

        pagamento.setTime(0L);

        assertEquals(0L, dto.getDataPagamento().getTime());
    }

    @Test
    @DisplayName("senza equals: due DTO con gli stessi dati non sono uguali")
    void senzaEquals() {
        assertNotEquals(completo(), completo());
    }

    @Test
    @DisplayName("senza @XmlRootElement: non si può serializzare in XML come elemento radice")
    void nonSerializzabileXml() {
        assertThrows(MarshalException.class, () ->
                JAXBContext.newInstance(RicevutaDTO.class)
                        .createMarshaller()
                        .marshal(completo(), new StringWriter()));
    }


    private static RicevutaDTO completo() {
        RicevutaDTO dto = new RicevutaDTO();
        dto.setCodiceFiscale("RSSMRA80A01F839X");
        dto.setTipoTributo("MULTA");
        dto.setIuv("IUV-1");
        dto.setIdTransazione("TRX-001");
        dto.setImporto(80.50);
        dto.setCodiceEnte("C_F839");
        dto.setEnteEmittente("Comune di Napoli");
        dto.setContattoEnte("tributi@comune.napoli.it");
        dto.setDataPagamento(PAGAMENTO);
        return dto;
    }
}