package com.msj.marketdata.domain.events;

import com.msj.shared.domain.DomainEvent;

public sealed interface MarketDataEvent extends DomainEvent
        permits PriceUpdatedEvent {}