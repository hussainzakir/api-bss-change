package com.trinet.ambis.service.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.StrategyHsaFunding;
import com.trinet.ambis.service.email.impl.EmailGenServiceImpl;
import com.trinet.ambis.service.model.ContributionHeader;
import com.trinet.ambis.service.model.ContributionPlan;
import com.trinet.ambis.service.model.StrategyBenefitGroup;

import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(MockitoJUnitRunner.class)
public class FreemarkerTemplateTest {

    private static final String FREEMARKER_TEMPLATE_MASTER_FILE = "FreemarkerTemplateTest.html";

    @InjectMocks
    private EmailGenServiceImpl templateService;

    @Mock
    private Configuration mockConfiguration;
    
    @Mock
    private PsDao psDao;

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setClassForTemplateLoading(EmailGenServiceImpl.class, "/freemarker");
    }

    @Test
    public void testTransform() throws IOException {

        Template template = configuration.getTemplate("emailConfirmation.ftl");
        when(mockConfiguration.getTemplate(anyString(), anyString())).thenReturn(template);

        String html = templateService.transform(populateDataModel(), true);
        String htmlTrimed = html.replaceAll("\\s","");
        
        String file = readFile(FREEMARKER_TEMPLATE_MASTER_FILE);
        String fileTrimed = file.replaceAll("\\s","");
        
        //assertEquals(htmlTrimed, fileTrimed);
    }

    private String readFile(String filePath) throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        String absolutePath = file.getAbsolutePath();

        StringBuilder contentBuilder = new StringBuilder();
        Stream<String> stream = Files.lines(Paths.get(absolutePath), StandardCharsets.UTF_8);
        stream.forEach(s -> contentBuilder.append(s).append("\n"));
        stream.close();
        
        return contentBuilder.toString();
    }

    private Map<String, Object> populateDataModel() {

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("debug", false);

        parameters.put("renewal", "true");
        parameters.put("serviceOrderNum", "ABC123");
        parameters.put("recipient", "Abbi");
        parameters.put("companyName", "TEVVA");
        parameters.put("uniqueId", "XYZ789");
        parameters.put("strategy", "The best one");
        parameters.put("salesOrder", "RTL456");
        parameters.put("startTillEndDate", "12/31/2020");
        parameters.put("endDate", "12/31/2021");
        parameters.put("exchange", 3);
        parameters.put("TriNetIVrule", 0);
        parameters.put("monthlyAnnual", "$22000");
        parameters.put("budget", "$234000");
        parameters.put("employeeCoverageCode", CoverageCodesEnums.COV_EMPLOYEE.getName());
        parameters.put("spouseCoverageCode", CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getName());
        parameters.put("childCoverageCode", CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getName());
        parameters.put("familyCovarageCode", CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getName());
        parameters.put("emailContentBaseUrl", "external.email.content.baseUrl");
        
        //HSA
        StrategyHsaFunding hsaFunding = new StrategyHsaFunding();
        hsaFunding.setOptionId(0);
        hsaFunding.setAnnualEeAmount(BigDecimal.valueOf(2000));
        hsaFunding.setAnnualFamilyAmount(BigDecimal.valueOf(4000));
        hsaFunding.setAnnualMonth(3);
        hsaFunding.setQuarterlyEeAmount(BigDecimal.valueOf(1000));
        hsaFunding.setQuarterlyFamilyAmount(BigDecimal.valueOf(5000));
        hsaFunding.setQ1Month(1);
        hsaFunding.setQ2Month(4);
        hsaFunding.setQ3Month(7);
        hsaFunding.setQ4Month(10);
        hsaFunding.setMonthlyEeAmount(BigDecimal.valueOf(3000));
        hsaFunding.setMonthlyFamilyAmount(BigDecimal.valueOf(6000));
        
        parameters.put("hsaFunding", hsaFunding);

        ContributionHeader ch = new ContributionHeader();
        ch.setFundingTypeDescription("fund");
        ch.setFundingTypeCode("FLT");
        ch.setIsFundingFlatMax("true");
        ch.setEmployeeFlatMax("employeeFlatMax");
        ch.setEmployeePercent("employeePercent");
        ch.setSpouseFlatMax("spouseFlatMax");
        ch.setEmployeePlusSpousePercent("employeePlusSpousePercent");
        ch.setEmployeePlusChildPercent("employeePlusChildPercent");
        ch.setEmployeePlusFamilyPercent("employeePlusFamilyPercent");
        ch.setChildFlatMax("childFlatMax");
        ch.setFamilyFlatMax("familyFlatMax");

        parameters.put("medicalContributionHeaders", Arrays.asList(ch));

        parameters.put("dentalContributionHeaders", Arrays.asList(ch));
        parameters.put("visionContributionHeaders", Arrays.asList(ch));
        parameters.put("additionalBenefitContributionHeaders", Arrays.asList(ch));

        parameters.put("hasMedical", true);
        parameters.put("hasDental", true);
        parameters.put("hasVision", true);
        parameters.put("hasAdditionalBenefits", true);

        List<List<ContributionPlan>> groupsMedicalPlanContribution = Lists.newArrayList();
        List<ContributionPlan> medicalPlanContributions = Lists.newArrayList();
        groupsMedicalPlanContribution.add(medicalPlanContributions);
        parameters.put("groupsMedicalPlanContribution", groupsMedicalPlanContribution);

        List<List<ContributionPlan>> groupsDentalPlanContribution = Lists.newArrayList();
        List<ContributionPlan> dentalPlanContributions = Lists.newArrayList();
        groupsDentalPlanContribution.add(dentalPlanContributions);
        parameters.put("groupsDentalPlanContribution", groupsDentalPlanContribution);

        List<List<ContributionPlan>> groupsVisionPlanContribution = Lists.newArrayList();
        List<ContributionPlan> visionPlanContributions = Lists.newArrayList();
        groupsVisionPlanContribution.add(visionPlanContributions);
        parameters.put("groupsVisionPlanContribution", groupsVisionPlanContribution);

        StrategyBenefitGroup bg = new StrategyBenefitGroup();
        bg.setName("my Group");
        bg.setWaitPeriodDescr("wait");
        parameters.put("benefitGroups", Arrays.asList(bg));

        return parameters;
    }
}
