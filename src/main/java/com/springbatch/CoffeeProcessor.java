package com.springbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CoffeeProcessor implements ItemProcessor<CoffeeEntity, CoffeeEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeProcessor.class);

    @Override
    public CoffeeEntity process(final CoffeeEntity coffee) {
        String brand = coffee.getBrand().toUpperCase();
        String origin = coffee.getOrigin().toUpperCase();
        String characteristic = coffee.getCharacteristics().toUpperCase();

        CoffeeEntity transformedCoffee = new CoffeeEntity(brand, origin, characteristic);
        LOGGER.info("Converting ( {} ) into ( {} )", coffee, transformedCoffee);

        return transformedCoffee;
    }

}
