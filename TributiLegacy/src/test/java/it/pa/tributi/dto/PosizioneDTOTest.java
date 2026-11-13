package it.pa.tributi.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PosizioneDTO")
class PosizioneDTOTest {

    private static final Date EMISSIONE = new Date(1700000000000L);
    private static final Date SCADENZA = new Date(1702592000000L);
    private static final Date PAGAMENTO = new Date(1701000000000L);

    @Test
    @DisplayName("un DTO nuovo ha tutti i campi null")
    void campiInizialiNull() {
        PosizioneDTO dto = new PosizioneDTO();

        assertNull(dto.getIuv());
        assertNull(dto.getCodiceFiscale());
        assertNull(dto.getTipoTributo());
        assertNull(dto.getImporto());
        assertNull(dto.getImportoOriginale());
        assertNull(dto.getStato());
        assertNull(dto.getDataEmissione());
        assertNull(dto.getDataScadenza());
        assertNull(dto.getDataPagamento());
        assertNull(dto.getCodiceEnte());
        assertNull(dto.getEnteEmittente());
        assertNull(dto.getContattoEnte());
    }

    @Test
    @DisplayName("ogni setter valorizza il proprio campo")
    void getterSetter() {
        PosizioneDTO dto = completo();

        assertEquals("IUV-1", dto.getIuv());
        assertEquals("RSSMRA80A01F839X", dto.getCodiceFiscale());
        assertEquals("TARI", dto.getTipoTributo());
        assertEquals(new BigDecimal("130.00"), dto.getImporto());
        assertEquals(new BigDecimal("100.00"), dto.getImportoOriginale());
        assertEquals("PAGATA", dto.getStato());
        assertEquals(EMISSIONE, dto.getDataEmissione());
        assertEquals(SCADENZA, dto.getDataScadenza());
        assertEquals(PAGAMENTO, dto.getDataPagamento());
        assertEquals("C_F839", dto.getCodiceEnte());
        assertEquals("Comune di Napoli", dto.getEnteEmittente());
        assertEquals("tributi@comune.napoli.it", dto.getContattoEnte());
    }

    @Test
    @DisplayName("le date sono condivise: modificare la Date originale modifica il DTO")
    void dateNonCopiate() {
        Date scadenza = new Date(SCADENZA.getTime());
        PosizioneDTO dto = new PosizioneDTO();
        dto.setDataScadenza(scadenza);

        scadenza.setTime(0L);

        assertEquals(0L, dto.getDataScadenza().getTime());
    }

    @Test
    @DisplayName("senza equals: due DTO con gli stessi dati non sono uguali")
    void senzaEquals() {
        assertNotEquals(completo(), completo());
    }

    @Test
    @DisplayName("XML: elemento radice posizioneDTO con i campi valorizzati")
    void serializzazioneXml() throws Exception {
        String xml = inXml(completo());

        assertTrue(xml.contains("<posizioneDTO>"));
        assertTrue(xml.contains("<iuv>IUV-1</iuv>"));
        assertTrue(xml.contains("<importo>130.00</importo>"));
        assertTrue(xml.contains("<enteEmittente>Comune di Napoli</enteEmittente>"));
    }

    @Test
    @DisplayName("XML: i campi null non compaiono")
    void campiNullAssentiInXml() throws Exception {
        PosizioneDTO dto = completo();
        dto.setDataPagamento(null);

        String xml = inXml(dto);

        assertFalse(xml.contains("dataPagamento"));
    }

    @Test
    @DisplayName("XML: andata e ritorno conservano tutti i campi")
    void andataERitornoXml() throws Exception {
        String xml = inXml(completo());

        PosizioneDTO letto = (PosizioneDTO) JAXBContext.newInstance(PosizioneDTO.class)
                .createUnmarshaller()
                .unmarshal(new StringReader(xml));

        assertEquals("IUV-1", letto.getIuv());
        assertEquals("RSSMRA80A01F839X", letto.getCodiceFiscale());
        assertEquals("TARI", letto.getTipoTributo());
        assertEquals(0, new BigDecimal("130.00").compareTo(letto.getImporto()));
        assertEquals(0, new BigDecimal("100.00").compareTo(letto.getImportoOriginale()));
        assertEquals("PAGATA", letto.getStato());
        assertEquals(EMISSIONE.getTime(), letto.getDataEmissione().getTime());
        assertEquals(SCADENZA.getTime(), letto.getDataScadenza().getTime());
        assertEquals(PAGAMENTO.getTime(), letto.getDataPagamento().getTime());
        assertEquals("C_F839", letto.getCodiceEnte());
        assertEquals("Comune di Napoli", letto.getEnteEmittente());
        assertEquals("tributi@comune.napoli.it", letto.getContattoEnte());
    }


    private static PosizioneDTO completo() {
        PosizioneDTO dto = new PosizioneDTO();
        dto.setIuv("IUV-1");
        dto.setCodiceFiscale("RSSMRA80A01F839X");
        dto.setTipoTributo("TARI");
        dto.setImporto(new BigDecimal("130.00"));
        dto.setImportoOriginale(new BigDecimal("100.00"));
        dto.setStato("PAGATA");
        dto.setDataEmissione(EMISSIONE);
        dto.setDataScadenza(SCADENZA);
        dto.setDataPagamento(PAGAMENTO);
        dto.setCodiceEnte("C_F839");
        dto.setEnteEmittente("Comune di Napoli");
        dto.setContattoEnte("tributi@comune.napoli.it");
        return dto;
    }

    private static String inXml(PosizioneDTO dto) throws Exception {
        Marshaller m = JAXBContext.newInstance(PosizioneDTO.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FRAGMENT, true); // senza intestazione <?xml ...?>
        StringWriter out = new StringWriter();
        m.marshal(dto, out);
        return out.toString();
    }
}