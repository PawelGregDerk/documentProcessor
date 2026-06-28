package by.vstu.isit.documentprocessor.dto;

public record FuncDto(
        Long id,
        String name,
        Long idOper,
        String param,
        Boolean isProd,
        String specCharakt
) {
}
