package com.example.fundraising.donation;

public class DonationNotFoundException extends RuntimeException{

    public DonationNotFoundException(Long id){
        super("could not find donation with id" + id);
    }
}
