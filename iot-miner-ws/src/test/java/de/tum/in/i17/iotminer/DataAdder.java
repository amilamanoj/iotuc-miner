package de.tum.in.i17.iotminer;

import de.tum.in.i17.iotminer.lib.data.Industry;
import de.tum.in.i17.iotminer.lib.data.IndustryRepository;
import de.tum.in.i17.iotminer.lib.data.UseCase;
import de.tum.in.i17.iotminer.lib.data.UseCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by amilamanoj on 24.06.17.
 */
    //@SpringBootApplication
    public class DataAdder implements CommandLineRunner {
        private static final Logger logger = LoggerFactory.getLogger(DataAdder.class);

        @Autowired
        private UseCaseRepository useCaseRepository;

        @Autowired
        private IndustryRepository industryRepository;

        public static void main(String[] args) {
            SpringApplication.run(DataAdder.class, args);
        }

        @Override
        @Transactional
        public void run(String... strings) throws Exception {
            // save a couple of books
            final Industry industryA = new Industry(0, "Automotive");
            final Industry industryB = new Industry(1, "Agriculture");
            final Industry industryC = new Industry(2, "IT");
            final Industry industryD = new Industry(3, "Energy");
            final Industry industryE = new Industry(4, "Construction");

            final UseCase useCaseA = new UseCase("AT&T #IoT Solution Helps Keep Beverage Coolers Stocked and Chilled Around the World");
            final UseCase useCaseB = new UseCase("Making factories more #intelligent");
            final UseCase useCaseC = new UseCase("Great #UX at the gas station: #IoT enables in-car-payment");
            final UseCase useCaseD = new UseCase("How to solve the water shortage problem using #Predix and how to reduce costs with #IoTSolution");
            final UseCase useCaseE = new UseCase("Adaptive #drone swarms share distributed decision-making brain");

            Set<UseCase> iauc = new HashSet<>();
            iauc.add(useCaseA);
            iauc.add(useCaseB);
            industryA.setUseCases(iauc);
            Set<UseCase> ibuc = new HashSet<>();
            ibuc.add(useCaseC);
            industryB.setUseCases(ibuc);
            Set<UseCase> iduc = new HashSet<>();
            iduc.add(useCaseD);
            iduc.add(useCaseE);
            industryD.setUseCases(iduc);

            useCaseA.setIndustry(industryA);
            useCaseB.setIndustry(industryA);
            useCaseC.setIndustry(industryB);
            useCaseD.setIndustry(industryD);
            useCaseE.setIndustry(industryD);

            useCaseRepository.save(useCaseA);
            useCaseRepository.save(useCaseB);
            useCaseRepository.save(useCaseC);
            useCaseRepository.save(useCaseD);
            useCaseRepository.save(useCaseE);

            for(UseCase useCase : useCaseRepository.findAll()) {
                logger.info(useCase.toString());
            }

        }
}
