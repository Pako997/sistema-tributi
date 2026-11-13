package it.pa.tributi.ws.handler;

import it.pa.tributi.context.UtenteContext;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

public class SpidHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String NS = "http://ws.tributi.pa.it/";

    @Override
    public boolean handleMessage(SOAPMessageContext ctx) {

        Boolean outbound = (Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (Boolean.TRUE.equals(outbound)) {
            UtenteContext.clear();
            return true;
        }

        try {
            SOAPHeader header = ctx.getMessage().getSOAPPart().getEnvelope().getHeader();
            if (header != null) {
                Iterator<?> it = header.getChildElements(new QName(NS, "codiceFiscale"));
                if (it.hasNext()) {
                    SOAPElement el = (SOAPElement) it.next();
                    UtenteContext.set(el.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext ctx) {
        return true;
    }

    @Override
    public void close(MessageContext ctx) {
        UtenteContext.clear();
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
