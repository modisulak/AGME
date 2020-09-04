/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wed18305.assignment1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Bean
// public WebMvcConfigurer corsConfigurer() {
// 	return new WebMvcConfigurer() {
// 		@Override
// 		public void addCorsMappings(CorsRegistry registry) {
// 			registry.addMapping("/**").allowedOrigins("http://localhost:3000");
// 		}
// 	};
// }
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**").allowedOrigins("http://localhost:3000").allowCredentials(true);
		}
}
