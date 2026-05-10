package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataOhlcvRepository extends MongoRepository<OhlcvDocument, String> {}