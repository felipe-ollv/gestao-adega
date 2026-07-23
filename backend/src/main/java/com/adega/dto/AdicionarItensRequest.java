package com.adega.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdicionarItensRequest(
        @NotEmpty List<@Valid AdicionarItemRequest> itens
) {
}
