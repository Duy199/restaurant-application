package com.example.RestaurantApplication.module.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public class RestaurantCreate {

    @NotBlank (message = "Restaurant name cannot be blank") 
    private String name;
    @NotBlank (message = "Restaurant address cannot be blank")
    private String address;

    public RestaurantCreate(String name, String address) {
        this.name = name;
        this.address = address;
    }


    public String getName() {
        return name;   
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
