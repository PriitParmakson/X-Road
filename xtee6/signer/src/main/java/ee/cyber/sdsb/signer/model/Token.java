package ee.cyber.sdsb.signer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.apache.commons.lang3.ObjectUtils;

import ee.cyber.sdsb.signer.protocol.dto.KeyInfo;
import ee.cyber.sdsb.signer.protocol.dto.TokenInfo;
import ee.cyber.sdsb.signer.protocol.dto.TokenStatusInfo;
import ee.cyber.sdsb.signer.tokenmanager.token.TokenType;

@Data
public final class Token {

    /** The module type as configured in Signer's module configuration. */
    private final String type;

    /** The token id. */
    private final String id;

    /** The module id. */
    private String moduleId;

    /** The name to display in UI. */
    private String friendlyName;

    /** True, if token is read-only */
    private boolean readOnly;

    /** True, if token is available (in module) */
    private boolean available;

    /** True, if password is inserted */
    private boolean active;

    /** The token serial number (optional). */
    private String serialNumber;

    /** The token label (optional). */
    private String label;

    /** The pin index to further specify the token (optional). */
    private int slotIndex;

    /** Whether batch signing should be enabled for this token. */
    private boolean batchSigningEnabled = false;

    /** Holds the currect status of the token. */
    private TokenStatusInfo status = TokenStatusInfo.OK;

    /** Contains the the keys of this token. */
    private final List<Key> keys = new ArrayList<>();

    /** Contains label-value pairs of information about token. */
    private final Map<String, String> tokenInfo = new LinkedHashMap<>();

    public void addKey(Key key) {
        keys.add(key);
    }

    public void setInfo(Map<String, String> tokenInfo) {
        this.tokenInfo.clear();
        this.tokenInfo.putAll(tokenInfo);
    }

    public TokenInfo toDTO() {
        return new TokenInfo(type, friendlyName, id, readOnly, available,
                active, serialNumber, label, slotIndex, status,
                Collections.unmodifiableList(getKeysAsDTOs()),
                Collections.unmodifiableMap(tokenInfo));
    }

    public boolean matches(TokenType token) {
        if (token == null) {
            return false;
        }

        if (id.equals(token.getId())) {
            return true;
        }

        return token.getModuleType() != null
                && token.getModuleType().equals(type)
                && ObjectUtils.equals(token.getSerialNumber(), serialNumber)
                && ObjectUtils.equals(token.getLabel(), label)
                && ObjectUtils.equals(token.getSlotIndex(), slotIndex);
    }

    private List<KeyInfo> getKeysAsDTOs() {
        List<KeyInfo> keyInfo = new ArrayList<>();
        for (Key key : keys) {
            keyInfo.add(key.toDTO());
        }

        return keyInfo;
    }
}