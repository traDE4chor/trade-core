package de.unistuttgart.iaas.trade.data.access;

/**
 * Created by hahnml on 25.10.2016.
 */
public interface IDataAccessService {

    byte[] readData(String protocol, String sourceURN);

    boolean writeData(String protocol, String targetURN, byte[] data);

}
