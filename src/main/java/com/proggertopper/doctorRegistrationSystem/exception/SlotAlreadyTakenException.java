package com.proggertopper.doctorRegistrationSystem.exception;

public class SlotAlreadyTakenException extends RuntimeException{

    public SlotAlreadyTakenException(String message){
        super(message);
    }
}
