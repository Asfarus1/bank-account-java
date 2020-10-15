package com.asfarus1.bankaccount.exceptions;

public class NotEnoughBalance extends RuntimeException{
    public NotEnoughBalance(String message){
        super(message);
    }
}
