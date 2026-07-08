package com.dispatch.schedulingapi.dto;

public record CustomerRequestDTO(
        String name,
        String email,
        String phone
) {}
