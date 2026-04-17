package com.rra.taxhandbook.content;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicReferenceDataContractIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void publicArticlesEndpointReturnsExpectedContract() throws Exception {
		mockMvc.perform(get("/api/articles"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("VAT Basics"))
			.andExpect(jsonPath("$[0].slug").value("vat-basics"))
			.andExpect(jsonPath("$[0].language").value("EN"))
			.andExpect(jsonPath("$[0].status").value("PUBLISHED"));
	}

	@Test
	void publicArticleSearchEndpointReturnsExpectedContract() throws Exception {
		mockMvc.perform(
			post("/api/articles/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "query": "vat"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].slug").value("vat-basics"))
			.andExpect(jsonPath("$[0].status").value("PUBLISHED"));
	}

	@Test
	void publicArticleSearchEndpointRejectsMalformedJson() throws Exception {
		mockMvc.perform(
			post("/api/articles/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{")
		)
			.andExpect(status().isBadRequest());
	}

	@Test
	void publicArticleSearchEndpointRejectsInvalidLanguage() throws Exception {
		mockMvc.perform(
			post("/api/articles/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "keyword": "vat",
					  "language": "DE"
					}
					""")
		)
			.andExpect(status().isBadRequest());
	}

	@Test
	void publicCategoriesEndpointReturnsExpectedContract() throws Exception {
		mockMvc.perform(get("/api/categories"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].name").value("Income Tax"))
			.andExpect(jsonPath("$[0].slug").value("income-tax"))
			.andExpect(jsonPath("$[0].language").value("EN"));
	}

	@Test
	void publicDocumentsEndpointReturnsExpectedContract() throws Exception {
		mockMvc.perform(get("/api/documents"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("Tax Handbook 2025"))
			.andExpect(jsonPath("$[0].fileName").value("RRA_Tax_Handbook_2025_Final.pdf"))
			.andExpect(jsonPath("$[0].fileUrl").value("/documents/tax-handbook-2025"));
	}

	@Test
	void publicFaqsEndpointReturnsExpectedContract() throws Exception {
		mockMvc.perform(get("/api/faqs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].question").value("How do I register for taxes?"))
			.andExpect(jsonPath("$[0].answer").value("You start with TIN registration."))
			.andExpect(jsonPath("$[0].language").value("EN"));
	}
}
