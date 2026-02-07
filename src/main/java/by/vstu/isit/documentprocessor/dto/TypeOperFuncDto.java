package by.vstu.isit.documentprocessor.dto;

public record TypeOperFuncDto(
        Long id,
        String name,
        Long idTypeOper,
        String param,
        Boolean isProd,
        String specCharakt
) {
}
