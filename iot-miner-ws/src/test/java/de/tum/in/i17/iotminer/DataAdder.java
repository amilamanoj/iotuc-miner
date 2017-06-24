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
            final Industry industryA = new Industry("Automotive", "Automotive Industry");
            final Industry industryB = new Industry("Agriculture", "Agriculture Industry");
            final Industry industryC = new Industry("Information technology", "IT Industry");
            final Industry industryD = new Industry("Energy", "Energy Industry");
            final Industry industryE = new Industry("Construction", "Construction Industry");

            final UseCase useCaseA = new UseCase("AT&T #IoT Solution Helps Keep Beverage Coolers Stocked and Chilled Around the World", "AT&T");
            final UseCase useCaseB = new UseCase("Making factories more #intelligent", "Microsoft");
            final UseCase useCaseC = new UseCase("Great #UX at the gas station: #IoT enables in-car-payment", "Shell");

            useCaseA.setIndustries(new HashSet<Industry>(){{
                add(industryA); }});
            useCaseB.setIndustries(new HashSet<Industry>(){{
                add(industryB);add(industryC); }});
            useCaseC.setIndustries(new HashSet<Industry>(){{
                add(industryD);add(industryE); }});

            useCaseRepository.save(useCaseA);
            useCaseRepository.save(useCaseB);
            useCaseRepository.save(useCaseC);


            // fetch all books
            for(UseCase useCase : useCaseRepository.findAll()) {
                logger.info(useCase.toString());
            }

//            // save a couple of publishers
//            final UseCase bookA = new UseCase("UseCase A");
//            final UseCase bookB = new UseCase("UseCase B");
//
//            industryRepository.save(new HashSet<Industry>() {{
//                add(new Industry("Industry A", new HashSet<UseCase>() {{
//                    add(bookA);
//                    add(bookB);
//                }}));
//
//                add(new Industry("Industry B", new HashSet<UseCase>() {{
//                    add(bookA);
//                    add(bookB);
//                }}));
//            }});
//
//            // fetch all publishers
//            for(Industry publisher : industryRepository.findAll()) {
//                logger.info(publisher.toString());
//            }
        }
}
