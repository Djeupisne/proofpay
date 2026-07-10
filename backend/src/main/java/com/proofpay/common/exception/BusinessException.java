package com.proofpay.common.exception;

/**
 * Exception métier levée quand une action est interdite par l'état courant
 * (ex : payer une transaction non acceptée, ouvrir un 2e litige actif...).
 * Toujours accompagnée d'un code métier stable exploitable par le front.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
