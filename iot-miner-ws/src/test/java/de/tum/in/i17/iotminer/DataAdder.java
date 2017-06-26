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
            final Industry industryF = new Industry("Healthcare", "Healthcare Industry");

            final UseCase useCaseA = new UseCase("AT&T #IoT Solution Helps Keep Beverage Coolers Stocked and Chilled Around the World", "AT&T");
            final UseCase useCaseB = new UseCase("Making factories more #intelligent", "Microsoft");
            final UseCase useCaseC = new UseCase("Great #UX at the gas station: #IoT enables in-car-payment", "Shell");
            final UseCase useCaseD = new UseCase("How to solve the water shortage problem using #Predix and how to reduce costs with #IoTSolution", "Predix");
            final UseCase useCaseE = new UseCase("Adaptive #drone swarms share distributed decision-making brain", "USGov");
            final UseCase useCaseF = new UseCase("@kwit_case uses an #iot cigarette case to provide data to gamify quitting smoking. ", "Kwit");

            useCaseA.setIndustries(new HashSet<Industry>(){{
                add(industryA);add(industryB); }});
            useCaseB.setIndustries(new HashSet<Industry>(){{
                add(industryB);add(industryC); }});
            useCaseC.setIndustries(new HashSet<Industry>(){{
                add(industryD);add(industryE); }});
            useCaseD.setIndustries(new HashSet<Industry>(){{
                add(industryB);add(industryD);add(industryE); }});
            useCaseE.setIndustries(new HashSet<Industry>(){{
                add(industryA);add(industryE); }});
            useCaseF.setIndustries(new HashSet<Industry>(){{
                add(industryD);add(industryF); }});

            useCaseRepository.save(useCaseA);
            useCaseRepository.save(useCaseB);
            useCaseRepository.save(useCaseC);
            useCaseRepository.save(useCaseD);
            useCaseRepository.save(useCaseE);
            useCaseRepository.save(useCaseF);


            for(UseCase useCase : useCaseRepository.findAll()) {
                logger.info(useCase.toString());
            }

        }
}
