/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package de.tum.in.i17.iotminer;

import de.tum.in.i17.iotminer.lib.data.Industry;
import de.tum.in.i17.iotminer.lib.data.IndustryRepository;
import de.tum.in.i17.iotminer.lib.data.UseCase;
import de.tum.in.i17.iotminer.lib.data.UseCaseRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/iot")
@Api(tags = {"iot"})
@CrossOrigin
public class WebController {

    private final UseCaseRepository useCaseRepository;
    private final IndustryRepository industryRepository;

    @Autowired
    public WebController(UseCaseRepository useCaseRepository,
                         IndustryRepository industryRepository) throws IOException {
        this.useCaseRepository = useCaseRepository;
        this.industryRepository = industryRepository;
    }


    @RequestMapping(value = "/usecases", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity getUseCases(
            @RequestParam(name = "industry", required = false) Integer industry,
            HttpServletResponse response)
            throws IllegalAccessException, URISyntaxException, ClassNotFoundException {

        List<UseCase> useCaseList = new ArrayList<>();
        Iterable<UseCase> useCaseIterable;
        if (industry != null) {
            useCaseIterable = useCaseRepository.findByIndustry_Id(industry);
        } else {
            useCaseIterable = useCaseRepository.findAll();
        }
        for (UseCase useCase : useCaseIterable) {
            useCaseList.add(useCase);
        }

        return ResponseEntity.status(HttpStatus.OK).body(useCaseList);

    }

    @RequestMapping(value = "/usecases/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity getUseCases(
            @PathVariable("id") long id)
            throws IllegalAccessException, URISyntaxException, ClassNotFoundException {

        UseCase useCase = useCaseRepository.findOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(useCase);
    }

    @RequestMapping(value = "/industries", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity getIndustries(
            @RequestParam(name = "input", required = false) String input,
            HttpServletResponse response)
            throws IllegalAccessException, URISyntaxException, ClassNotFoundException {

        List<Industry> industryList = new ArrayList<>();
        for (Industry useCase : industryRepository.findAll()) {
            industryList.add(useCase);
        }

        return ResponseEntity.status(HttpStatus.OK).body(industryList);

    }




}
