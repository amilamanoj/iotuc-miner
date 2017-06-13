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

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "/iot")
@Api(tags = {"iot"})
public class WebController {

    public WebController() throws IOException {
    }


    @RequestMapping(value = "/usecases", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity getVariantsByRegionStreamingOutput(
            @RequestParam(name = "input", required = false) String input,
            HttpServletResponse response)
            throws IllegalAccessException, URISyntaxException, ClassNotFoundException {

        String res = "res_" + input;

        return ResponseEntity.status(HttpStatus.OK).body(res);

    }


}
