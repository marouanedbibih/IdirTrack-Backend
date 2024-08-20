package com.idirtrack.backend.operator;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorRequest {

    @NotEmpty(message = "Operator must not be empty")
    private String name;
}
