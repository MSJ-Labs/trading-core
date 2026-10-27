package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.application.port.out.PriceTickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersistPriceTickCommandHandler implements PersistPriceTickUseCase {

    private final PriceTickRepository priceTickRepository;

    @Override
    public void handle(PersistPriceTickCommand command) {
        priceTickRepository.save(new PriceUpdate(command.symbol(), command.price(), command.timestamp()));
    }
}