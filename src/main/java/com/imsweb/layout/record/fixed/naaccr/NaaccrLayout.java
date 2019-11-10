/*
 * Copyright (C) 2013 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed.naaccr;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.imsweb.layout.Field;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutFieldXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutXmlDto;

/**
 * This class contains the logic related to all NAACCR layouts.
 */
public class NaaccrLayout extends FixedColumnsLayout {

    //  following styles are used in all the NAACCR versions
    private static final StringBuilder _CSS_STYLE_SUMMARY_TABLE = new StringBuilder();

    static {
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-table { width: 100%; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-header { text-align: center; padding: 2px; background-color: #E0E0E0; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-cell { vertical-align:top; padding: 2px; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-centered { text-align: center; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-borders { border: 1px solid gray; border-collapse: collapse; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-underline { text-decoration:underline; }\n");
    }

    // following styles are used only in version 13 and prior
    private static final StringBuilder _CSS_STYLE_13_AND_PRIOR = new StringBuilder();

    static {
        _CSS_STYLE_13_AND_PRIOR.append(".naaccr-codes-cell { text-align: left; vertical-align:top; padding: 2px; padding-left: 5px; padding-right: 5px }\n");
        _CSS_STYLE_13_AND_PRIOR.append(".naaccr-codes-padding { padding-right: 40px; }\n");
        _CSS_STYLE_13_AND_PRIOR.append(".naaccr-codes-nowrap { white-space: nowrap; }\n");
    }

    // following styles are used only in version 14 and later (those come straight from the online NAACCR HTML page)
    private static final StringBuilder _DEFAULT_CSS_14_AND_LATER = new StringBuilder();

    static {
        _DEFAULT_CSS_14_AND_LATER.append("#readerWrapper { margin-top: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#pnlSearch { width: 100%;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#lblSearch { margin-right: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".wrapper { margin-bottom: 50px; width: 775px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(
                ".col { border-bottom: 1px solid black; border-left: 1px solid black; border-top: 1px solid black; float: left; height: 1100px; margin-bottom: 50px; padding: 0 5px; width: 180px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(
                ".colnoborder { border-bottom: 1px solid black; border-top: 1px solid black; float: left; height: 1100px; margin-bottom: 50px; padding: 0 5px; width: 180px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColHead td:first-child { text-align: left;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColBody { text-align: center; vertical-align: top;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColTitle { background-color: #165185; color: #FABA44;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColDataStripe td:first-child { border-left: 0 none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColDataStripe table tr td { padding: 0; text-align: center;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColData td:first-child { border-left: 0 none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColData table tr td { padding: 0; text-align: center;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColData td.HighlightToolTip,\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColDataStripe td.HighlightToolTip { border-bottom: 1px dashed black; background-color: #a7d9f2;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableAppendixBody { border-left: 0 none; display: table-row; padding-bottom: 1px; text-align: left;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".code-nbr { padding-right: 20px; vertical-align: top;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#stateList { padding-bottom: 30px; text-align: center; width: 720px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#chapter { width: 950px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder { display: none; left: 0; position: absolute; top: 0; z-index: 99999;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top { padding-bottom: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom { padding-top: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_right { padding-left: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_left { padding-right: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tblSearch { margin-top: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".dataDictionaryHeader { padding-right: 10px; text-align: right;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#menuWrapper { height: 200px; margin-top: 20px; width: 950px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chapterColumn { border-right: 2px solid white; float: left;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("a .chapter { color: #FFFFFF; padding: 0; text-decoration: none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chapter:hover { background: none repeat scroll 0 0 #FF9900; color: #FFFFFF; text-decoration: none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_arrow, #tiptip_arrow_inner { border-color: transparent; border-style: solid; border-width: 6px; height: 0; position: absolute; width: 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top #tiptip_arrow { border-top-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom #tiptip_arrow { border-bottom-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_right #tiptip_arrow { border-right-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_left #tiptip_arrow { border-left-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top #tiptip_arrow_inner { border-top-color: #FFFFCE; margin-left: -6px; margin-top: -7px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom #tiptip_arrow_inner { border-bottom-color: #FFFFCE; margin-left: -6px; margin-top: -5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_right #tiptip_arrow_inner { border-right-color: #FFFFCE; margin-left: -5px; margin-top: -6px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_left #tiptip_arrow_inner { border-left-color: #FFFFCE; margin-left: -7px; margin-top: -6px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".GeoCtrAlpha { float: left; width: 300px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".threeColSubTitle { float: left; font-style: italic; margin-bottom: 12px; width: 300px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".colRight { border-bottom: 1px solid black; border-left: 1px solid black; float: right; height: 1150px; padding-left: 20px; width: 454px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".colLeft { border-bottom: 1px solid black; float: left; height: 1150px; padding-left: 20px; width: 455px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".GeoCtr { float: left; width: 460px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".threeCol { border-bottom: 1px solid black; float: left; height: 1135px; padding-left: 10px; width: 305px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".threeColTitle { float: left; font-weight: bold; width: 300px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".CountryContinentName { float: left; font-weight: bold; margin-bottom: 15px; margin-top: 15px; width: 400px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(
                ".threeColMid { -moz-border-bottom-colors: none; -moz-border-image: none; -moz-border-left-colors: none; -moz-border-right-colors: none; -moz-border-top-colors: none; border-color: -moz-use-text-color black black; border-style: none solid solid; border-width: medium 1px 1px; float: left; height: 1135px; padding-left: 10px; width: 305px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_content { background-color: #FFFFCE; padding: 4px 8px 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom #tiptip_arrow_inner { border-bottom-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top #tiptip_arrow_inner { border-top-color: rgba(20, 20, 20, 0.92);}\n");
        _DEFAULT_CSS_14_AND_LATER.append("h1 { margin: 1em 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("h2, h3 { margin: 0 0 1.33em 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("h4 { margin: 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul, ol { margin: 0 0 1.33em 35px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul li, ol li { margin-bottom: 1.33em;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("table.padded td, table.padded th { padding: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul.nobullets { list-style: none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul.notspaced li, ol.notspaced li { margin-bottom: 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".c8cell1 { width: 70px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#pnlSearch { width: 100%;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#lblSearch { margin-right: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-head-table { margin-top: 15px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-para-head { font-weight: bold; padding-top: 10px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-para { padding-bottom: 10px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-para ul li { margin-bottom: 0;}\n");
    }

    // mapping between the NAACCR XML IDs and the old (deprecated) layout names
    private static Map<String, String> _XML_TO_LAYOUT_MAPPING = new HashMap<>();

    static {
        _XML_TO_LAYOUT_MAPPING.put("addrAtDxCity", "addressAtDxCity");
        _XML_TO_LAYOUT_MAPPING.put("addrAtDxCountry", "addressAtDxCountry");
        _XML_TO_LAYOUT_MAPPING.put("addrAtDxNoStreet", "addressAtDxStreetName");
        _XML_TO_LAYOUT_MAPPING.put("addrAtDxPostalCode", "addressAtDxPostalCode");
        _XML_TO_LAYOUT_MAPPING.put("addrAtDxState", "addressAtDxState");
        _XML_TO_LAYOUT_MAPPING.put("addrAtDxSupplementl", "addressAtDxSupplementl");
        _XML_TO_LAYOUT_MAPPING.put("addrCurrentCity", "addressCurrentCity");
        _XML_TO_LAYOUT_MAPPING.put("addrCurrentCountry", "addressCurrentCountry");
        _XML_TO_LAYOUT_MAPPING.put("addrCurrentNoStreet", "addressCurrentStreetName");
        _XML_TO_LAYOUT_MAPPING.put("addrCurrentPostalCode", "addressCurrentPostalCode");
        _XML_TO_LAYOUT_MAPPING.put("addrCurrentState", "addressCurrentState");
        _XML_TO_LAYOUT_MAPPING.put("addrCurrentSupplementl", "addressCurrentSupplementl");
        _XML_TO_LAYOUT_MAPPING.put("ageAtDiagnosis", "ageAtDx");
        _XML_TO_LAYOUT_MAPPING.put("behaviorCodeIcdO3", "behaviorIcdO3");
        _XML_TO_LAYOUT_MAPPING.put("bilirubinPretxTotalLabValue", "bilirubinPretreatmentTotalLabValue");
        _XML_TO_LAYOUT_MAPPING.put("bilirubinPretxUnitOfMeasure", "bilirubinPretreatmentUnitOfMeasure");
        _XML_TO_LAYOUT_MAPPING.put("birthplace", "birthPlace");
        _XML_TO_LAYOUT_MAPPING.put("censusBlockGrp197090", "censusBlockGroup708090");
        _XML_TO_LAYOUT_MAPPING.put("censusCodSys19708090", "censusCodingSys708090");
        _XML_TO_LAYOUT_MAPPING.put("censusIndCode19702000", "industryCodeCensus");
        _XML_TO_LAYOUT_MAPPING.put("censusOccCode19702000", "occupationCodeCensus");
        _XML_TO_LAYOUT_MAPPING.put("censusOccIndSys7000", "occupationIndustryCodingSys");
        _XML_TO_LAYOUT_MAPPING.put("censusTrCert19708090", "censusCertainty708090");
        _XML_TO_LAYOUT_MAPPING.put("censusTrCertainty2000", "censusCertainty2000");
        _XML_TO_LAYOUT_MAPPING.put("censusTrCertainty2010", "censusCertainty2010");
        _XML_TO_LAYOUT_MAPPING.put("censusTrPovertyIndictr", "censusPovertyIndictr");
        _XML_TO_LAYOUT_MAPPING.put("censusTract19708090", "censusTract708090");
        _XML_TO_LAYOUT_MAPPING.put("chromosome19qLossHeterozygosity", "chromosome19qLossOfHeterozygosity");
        _XML_TO_LAYOUT_MAPPING.put("chromosome1pLossHeterozygosity", "chromosome1pLossOfHeterozygosity");
        _XML_TO_LAYOUT_MAPPING.put("codingSystemForEod", "eodCodingSys");
        _XML_TO_LAYOUT_MAPPING.put("countyAtDx", "addressAtDxCounty");
        _XML_TO_LAYOUT_MAPPING.put("countyAtDxGeocode1990", "countyDxGeocode1990");
        _XML_TO_LAYOUT_MAPPING.put("countyAtDxGeocode2000", "countyDxGeocode2000");
        _XML_TO_LAYOUT_MAPPING.put("countyAtDxGeocode2010", "countyDxGeocode2010");
        _XML_TO_LAYOUT_MAPPING.put("countyAtDxGeocode2020", "countyDxGeocode2020");
        _XML_TO_LAYOUT_MAPPING.put("countyCurrent", "addressCurrentCounty");
        _XML_TO_LAYOUT_MAPPING.put("creatininePretxUnitOfMeasure", "creatininePretreatmentUnitOfMeasure");
        _XML_TO_LAYOUT_MAPPING.put("csPostrxExtension", "csPostRxExt");
        _XML_TO_LAYOUT_MAPPING.put("csPostrxLymphNodes", "csPostRxLymphNodes");
        _XML_TO_LAYOUT_MAPPING.put("csPostrxMetsAtDx", "csPostRxMetsAtDx");
        _XML_TO_LAYOUT_MAPPING.put("csPostrxTumorSize", "csPostRxTumorSize");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxExtension", "csPreRxExt");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxLymphNodes", "csPreRxLymphNodes");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxMetsAtDx", "csPreRxMetsAtDx");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxMetsEval", "csPreRxMetsEval");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxRegNodesEval", "csPreRxRegNodesEval");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxTumSzExtEval", "csPreRxSizeExtEval");
        _XML_TO_LAYOUT_MAPPING.put("csPrerxTumorSize", "csPreRxTumorSize");
        _XML_TO_LAYOUT_MAPPING.put("csVersionInputOriginal", "csVersionOriginal");
        _XML_TO_LAYOUT_MAPPING.put("date1stCrsRxCoc", "dateOf1stCrsRxCoc");
        _XML_TO_LAYOUT_MAPPING.put("date1stCrsRxCocDay", "dateOf1stCrsRxCocDay");
        _XML_TO_LAYOUT_MAPPING.put("date1stCrsRxCocFlag", "dateOf1stCrsRxCocFlag");
        _XML_TO_LAYOUT_MAPPING.put("date1stCrsRxCocMonth", "dateOf1stCrsRxCocMonth");
        _XML_TO_LAYOUT_MAPPING.put("date1stCrsRxCocYear", "dateOf1stCrsRxCocYear");
        _XML_TO_LAYOUT_MAPPING.put("dateInitialRxSeer", "dateOfInitialRx");
        _XML_TO_LAYOUT_MAPPING.put("dateInitialRxSeerDay", "dateOfInitialRxDay");
        _XML_TO_LAYOUT_MAPPING.put("dateInitialRxSeerFlag", "dateOfInitialRxFlag");
        _XML_TO_LAYOUT_MAPPING.put("dateInitialRxSeerMonth", "dateOfInitialRxMonth");
        _XML_TO_LAYOUT_MAPPING.put("dateInitialRxSeerYear", "dateOfInitialRxYear");
        _XML_TO_LAYOUT_MAPPING.put("dateOfBirth", "birthDate");
        _XML_TO_LAYOUT_MAPPING.put("dateOfBirthDay", "birthDateDay");
        _XML_TO_LAYOUT_MAPPING.put("dateOfBirthFlag", "birthDateFlag");
        _XML_TO_LAYOUT_MAPPING.put("dateOfBirthMonth", "birthDateMonth");
        _XML_TO_LAYOUT_MAPPING.put("dateOfBirthYear", "birthDateYear");
        _XML_TO_LAYOUT_MAPPING.put("dateOfMultTumors", "dateOfMultipleTumors");
        _XML_TO_LAYOUT_MAPPING.put("dateOfMultTumorsDay", "dateOfMultipleTumorsDay");
        _XML_TO_LAYOUT_MAPPING.put("dateOfMultTumorsFlag", "dateOfMultipleTumorsFlag");
        _XML_TO_LAYOUT_MAPPING.put("dateOfMultTumorsMonth", "dateOfMultipleTumorsMonth");
        _XML_TO_LAYOUT_MAPPING.put("dateOfMultTumorsYear", "dateOfMultipleTumorsYear");
        _XML_TO_LAYOUT_MAPPING.put("dateRegionalLNDissection", "dateRegionalLymphNodeDissection");
        _XML_TO_LAYOUT_MAPPING.put("dateRegionalLNDissectionDay", "dateRegionalLymphNodeDissectionDay");
        _XML_TO_LAYOUT_MAPPING.put("dateRegionalLNDissectionFlag", "dateRegionalLymphNodeDissectionFlag");
        _XML_TO_LAYOUT_MAPPING.put("dateRegionalLNDissectionMonth", "dateRegionalLymphNodeDissectionMonth");
        _XML_TO_LAYOUT_MAPPING.put("dateRegionalLNDissectionYear", "dateRegionalLymphNodeDissectionYear");
        _XML_TO_LAYOUT_MAPPING.put("dateSentinelLymphNodeBiopsy", "dateOfSentinelLymphNodeBiopsy");
        _XML_TO_LAYOUT_MAPPING.put("dateSentinelLymphNodeBiopsyDay", "dateOfSentinelLymphNodeBiopsyDay");
        _XML_TO_LAYOUT_MAPPING.put("dateSentinelLymphNodeBiopsyMonth", "dateOfSentinelLymphNodeBiopsyMonth");
        _XML_TO_LAYOUT_MAPPING.put("dateSentinelLymphNodeBiopsyYear", "dateOfSentinelLymphNodeBiopsyYear");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc6MDescript", "derivedAjcc6MDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc6NDescript", "derivedAjcc6NDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc6StageGrp", "derivedAjcc6StageGroup");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc6TDescript", "derivedAjcc6TDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc7MDescript", "derivedAjcc7MDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc7NDescript", "derivedAjcc7NDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc7StageGrp", "derivedAjcc7StageGroup");
        _XML_TO_LAYOUT_MAPPING.put("derivedAjcc7TDescript", "derivedAjcc7TDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedNeoadjuvRxFlag", "derivedNeoAdJuvRxFlag");
        _XML_TO_LAYOUT_MAPPING.put("derivedPostrx7M", "derivedPostRx7M");
        _XML_TO_LAYOUT_MAPPING.put("derivedPostrx7N", "derivedPostRx7N");
        _XML_TO_LAYOUT_MAPPING.put("derivedPostrx7StgeGrp", "derivedPostRx7StageGroup");
        _XML_TO_LAYOUT_MAPPING.put("derivedPostrx7T", "derivedPostRx7T");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7M", "derivedPreRx7M");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7MDescrip", "derivedPreRx7MDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7N", "derivedPreRx7N");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7NDescrip", "derivedPreRx7NDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7StageGrp", "derivedPreRx7StageGroup");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7T", "derivedPreRx7T");
        _XML_TO_LAYOUT_MAPPING.put("derivedPrerx7TDescrip", "derivedPreRx7TDescriptor");
        _XML_TO_LAYOUT_MAPPING.put("derivedSeerCmbMSrc", "derivedSeerCombinedMSrc");
        _XML_TO_LAYOUT_MAPPING.put("derivedSeerCmbNSrc", "derivedSeerCombinedNSrc");
        _XML_TO_LAYOUT_MAPPING.put("derivedSeerCmbStgGrp", "derivedSeerCombinedStgGrp");
        _XML_TO_LAYOUT_MAPPING.put("derivedSeerCmbTSrc", "derivedSeerCombinedTSrc");
        _XML_TO_LAYOUT_MAPPING.put("derivedSs2017", "derivedSumStg2017");
        _XML_TO_LAYOUT_MAPPING.put("directlyAssignedSs2017", "directlyAssignedSumStg2017");
        _XML_TO_LAYOUT_MAPPING.put("estrogenReceptorPercntPosOrRange", "estrogenReceptorPercentPositiveOrRange");
        _XML_TO_LAYOUT_MAPPING.put("extentOfDisease10Dig", "extentOfDisease10Digit");
        _XML_TO_LAYOUT_MAPPING.put("extranodalExtensionHeadNeckClin", "extranodalExtensionHeadAndNeckClinical");
        _XML_TO_LAYOUT_MAPPING.put("extranodalExtensionHeadNeckPath", "extranodalExtensionHeadAndNeckPathological");
        _XML_TO_LAYOUT_MAPPING.put("followUpContactNost", "followUpContactNoAndStreet");
        _XML_TO_LAYOUT_MAPPING.put("gestationalTrophoblasticPxIndex", "gestationalTrophoblasticPrognosticScoringIndex");
        _XML_TO_LAYOUT_MAPPING.put("gisCoordinateQuality", "gisQuality");
        _XML_TO_LAYOUT_MAPPING.put("gradePathSystem", "gradePathSys");
        _XML_TO_LAYOUT_MAPPING.put("histologicTypeIcdO3", "histologyIcdO3");
        _XML_TO_LAYOUT_MAPPING.put("iNRProthrombinTime", "internationalNormalizedRatioForProthrombinTime");
        _XML_TO_LAYOUT_MAPPING.put("ihsLink", "ihs");
        _XML_TO_LAYOUT_MAPPING.put("ipsilateralAdrenalGlandInvolve", "ipsilateralAdrenalGlandInvolvement");
        _XML_TO_LAYOUT_MAPPING.put("lnAssessMethodFemoralInguinal", "lnAssessmentMethodFemoralInguinal");
        _XML_TO_LAYOUT_MAPPING.put("lnAssessMethodParaaortic", "lnAssessmentMethodParaAortic");
        _XML_TO_LAYOUT_MAPPING.put("lnAssessMethodPelvic", "lnAssessmentMethodPelvic");
        _XML_TO_LAYOUT_MAPPING.put("lnDistantAssessMethod", "lnDistantAssessmentMethod");
        _XML_TO_LAYOUT_MAPPING.put("lnStatusFemorInguinParaaortPelv", "lnStatusFemoralInguinalParaAorticPelvic");
        _XML_TO_LAYOUT_MAPPING.put("methylationOfO6MGMT", "methylationOfO6MethylguanineMethyltransferase");
        _XML_TO_LAYOUT_MAPPING.put("metsAtDxBone", "metsDxBone");
        _XML_TO_LAYOUT_MAPPING.put("metsAtDxBrain", "metsDxBrain");
        _XML_TO_LAYOUT_MAPPING.put("metsAtDxDistantLn", "metsDxDistantLn");
        _XML_TO_LAYOUT_MAPPING.put("metsAtDxLiver", "metsDxLiver");
        _XML_TO_LAYOUT_MAPPING.put("metsAtDxLung", "metsDxLung");
        _XML_TO_LAYOUT_MAPPING.put("metsAtDxOther", "metsDxOther");
        _XML_TO_LAYOUT_MAPPING.put("morphCodingSysOriginl", "morphCodingSysOriginal");
        _XML_TO_LAYOUT_MAPPING.put("morphIcdO1", "morphologyIcdO1");
        _XML_TO_LAYOUT_MAPPING.put("morphTypebehavIcdO2", "morphologyIcdO2");
        _XML_TO_LAYOUT_MAPPING.put("morphTypebehavIcdO3", "morphologyIcdO3");
        _XML_TO_LAYOUT_MAPPING.put("multTumRptAsOnePrim", "multiTumorRptAsOnePrim");
        _XML_TO_LAYOUT_MAPPING.put("nhiaDerivedHispOrigin", "nhia");
        _XML_TO_LAYOUT_MAPPING.put("npcrDerivedAjcc8TnmPostStgGrp", "npcrDerivedAjcc8TnmPostTherapyStgGrp");
        _XML_TO_LAYOUT_MAPPING.put("npiArchiveFin", "archiveFinNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiFollowingRegistry", "followingRegistryNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiInstReferredFrom", "institutionReferredFromNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiInstReferredTo", "institutionReferredToNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiPhysician3", "physician3Npi");
        _XML_TO_LAYOUT_MAPPING.put("npiPhysician4", "physician4Npi");
        _XML_TO_LAYOUT_MAPPING.put("npiPhysicianFollowUp", "physicianFollowUpNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiPhysicianManaging", "physicianManagingNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiPhysicianPrimarySurg", "physicianPrimarySurgNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiRegistryId", "registryIdNpi");
        _XML_TO_LAYOUT_MAPPING.put("npiReportingFacility", "reportingFacilityNpi");
        _XML_TO_LAYOUT_MAPPING.put("numberPhasesOfRadTxToVolume", "numberOfPhasesOfRadTreatmentToThisVolume");
        _XML_TO_LAYOUT_MAPPING.put("oncotypeDxRecurrenceScoreInvasiv", "oncotypeDxRecurrenceScoreInvasive");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs1", "overrideCs1");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs10", "overrideCs10");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs11", "overrideCs11");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs12", "overrideCs12");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs13", "overrideCs13");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs14", "overrideCs14");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs15", "overrideCs15");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs16", "overrideCs16");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs17", "overrideCs17");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs18", "overrideCs18");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs19", "overrideCs19");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs2", "overrideCs2");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs20", "overrideCs20");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs3", "overrideCs3");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs4", "overrideCs4");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs5", "overrideCs5");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs6", "overrideCs6");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs7", "overrideCs7");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs8", "overrideCs8");
        _XML_TO_LAYOUT_MAPPING.put("overRideCs9", "overrideCs9");
        _XML_TO_LAYOUT_MAPPING.put("overRideHospseqDxconf", "overRideHospSeqDxConf");
        _XML_TO_LAYOUT_MAPPING.put("overRideHospseqSite", "overRideHospSeqSite");
        _XML_TO_LAYOUT_MAPPING.put("overRideSeqnoDxconf", "overRideSeqNoDxConf");
        _XML_TO_LAYOUT_MAPPING.put("overRideSiteEodDxDt", "overRideSiteEodDxDate");
        _XML_TO_LAYOUT_MAPPING.put("overRideSiteLatSeqno", "overRideSiteLatSeqNo");
        _XML_TO_LAYOUT_MAPPING.put("overRideSiteTnmStggrp", "overRideSiteTnmStgGrp");
        _XML_TO_LAYOUT_MAPPING.put("overRideSsNodespos", "overRideSsNodesPos");
        _XML_TO_LAYOUT_MAPPING.put("overRideSurgDxconf", "overRideSurgDxConf");
        _XML_TO_LAYOUT_MAPPING.put("pathReportingFacId1", "pathReportFacId1");
        _XML_TO_LAYOUT_MAPPING.put("pathReportingFacId2", "pathReportFacId2");
        _XML_TO_LAYOUT_MAPPING.put("pathReportingFacId3", "pathReportFacId3");
        _XML_TO_LAYOUT_MAPPING.put("pathReportingFacId4", "pathReportFacId4");
        _XML_TO_LAYOUT_MAPPING.put("pathReportingFacId5", "pathReportFacId5");
        _XML_TO_LAYOUT_MAPPING.put("patientSystemIdHosp", "patientSysIdHospital");
        _XML_TO_LAYOUT_MAPPING.put("pediatricStagingSystem", "pediatricStagingSys");
        _XML_TO_LAYOUT_MAPPING.put("phase1RadiationExternalBeamTech", "phase1RadiationExternalBeamPlanningTech");
        _XML_TO_LAYOUT_MAPPING.put("phase1RadiationPrimaryTxVolume", "phase1RadiationPrimaryTreatmentVolume");
        _XML_TO_LAYOUT_MAPPING.put("phase1RadiationToDrainingLN", "phase1RadiationToDrainingLymphNodes");
        _XML_TO_LAYOUT_MAPPING.put("phase2RadiationExternalBeamTech", "phase2RadiationExternalBeamPlanningTech");
        _XML_TO_LAYOUT_MAPPING.put("phase2RadiationPrimaryTxVolume", "phase2RadiationPrimaryTreatmentVolume");
        _XML_TO_LAYOUT_MAPPING.put("phase2RadiationToDrainingLN", "phase2RadiationToDrainingLymphNodes");
        _XML_TO_LAYOUT_MAPPING.put("phase3RadiationExternalBeamTech", "phase3RadiationExternalBeamPlanningTech");
        _XML_TO_LAYOUT_MAPPING.put("phase3RadiationPrimaryTxVolume", "phase3RadiationPrimaryTreatmentVolume");
        _XML_TO_LAYOUT_MAPPING.put("phase3RadiationToDrainingLN", "phase3RadiationToDrainingLymphNodes");
        _XML_TO_LAYOUT_MAPPING.put("progesteroneRecepPrcntPosOrRange", "progesteroneReceptorPercentPositiveOrRange");
        _XML_TO_LAYOUT_MAPPING.put("progesteroneRecepSummary", "progesteroneReceptorSummary");
        _XML_TO_LAYOUT_MAPPING.put("progesteroneRecepTotalAllredScor", "progesteroneReceptorTotalAllredScore");
        _XML_TO_LAYOUT_MAPPING.put("raceNapiia", "napiia");
        _XML_TO_LAYOUT_MAPPING.put("radiationTxDiscontinuedEarly", "radiationTreatmentDiscontinuedEarly");
        _XML_TO_LAYOUT_MAPPING.put("residualTumVolPostCytoreduction", "residualTumorVolumePostCytoreduction");
        _XML_TO_LAYOUT_MAPPING.put("ruralurbanContinuum1993", "ruralUrbanContinuum1993");
        _XML_TO_LAYOUT_MAPPING.put("ruralurbanContinuum2003", "ruralUrbanContinuum2003");
        _XML_TO_LAYOUT_MAPPING.put("ruralurbanContinuum2013", "ruralUrbanContinuum2013");
        _XML_TO_LAYOUT_MAPPING.put("rxCodingSystemCurrent", "rxCodingSysCurrent");
        _XML_TO_LAYOUT_MAPPING.put("rxSummSurgicalApproch", "rxSummSurgicalApproach");
        _XML_TO_LAYOUT_MAPPING.put("rxSummSystemicSurSeq", "rxSummSystemicSurgSeq");
        _XML_TO_LAYOUT_MAPPING.put("rxTextRadiation", "rxTextRadiationBeam");
        _XML_TO_LAYOUT_MAPPING.put("seerSiteSpecificFact1", "seerSiteSpecificFactor1");
        _XML_TO_LAYOUT_MAPPING.put("seerSiteSpecificFact2", "seerSiteSpecificFactor2");
        _XML_TO_LAYOUT_MAPPING.put("seerSiteSpecificFact3", "seerSiteSpecificFactor3");
        _XML_TO_LAYOUT_MAPPING.put("seerSiteSpecificFact4", "seerSiteSpecificFactor4");
        _XML_TO_LAYOUT_MAPPING.put("seerSiteSpecificFact5", "seerSiteSpecificFactor5");
        _XML_TO_LAYOUT_MAPPING.put("seerSiteSpecificFact6", "seerSiteSpecificFactor6");
        _XML_TO_LAYOUT_MAPPING.put("sequenceNumberHospital", "sequenceNumberHosp");
        _XML_TO_LAYOUT_MAPPING.put("serumBeta2MicroglobulinPretxLvl", "serumBeta2MicroglobulinPretreatmentLevel");
        _XML_TO_LAYOUT_MAPPING.put("siteIcdO1", "primarySiteIcdO1");
        _XML_TO_LAYOUT_MAPPING.put("subsqRx2ndcrsDateFlag", "subsqRx2ndCourseDateFlag");
        _XML_TO_LAYOUT_MAPPING.put("subsqRx3rdcrsDateFlag", "subsqRx3rdCourseDateFlag");
        _XML_TO_LAYOUT_MAPPING.put("subsqRx4thcrsDateFlag", "subsqRx4thCourseDateFlag");
        _XML_TO_LAYOUT_MAPPING.put("survDateActiveFollowup", "survDateActiveFollowUp");
        _XML_TO_LAYOUT_MAPPING.put("survDateActiveFollowupDay", "survDateActiveFollowUpDay");
        _XML_TO_LAYOUT_MAPPING.put("survDateActiveFollowupMonth", "survDateActiveFollowUpMonth");
        _XML_TO_LAYOUT_MAPPING.put("survDateActiveFollowupYear", "survDateActiveFollowUpYear");
        _XML_TO_LAYOUT_MAPPING.put("survFlagActiveFollowup", "survFlagActiveFollowUp");
        _XML_TO_LAYOUT_MAPPING.put("survMosActiveFollowup", "survMonthsActiveFollowUp");
        _XML_TO_LAYOUT_MAPPING.put("survMosPresumedAlive", "survMonthsPresumedAlive");
        _XML_TO_LAYOUT_MAPPING.put("textPlaceOfDiagnosis", "placeOfDiagnosis");
        _XML_TO_LAYOUT_MAPPING.put("visceralParietalPleuralInvasion", "visceralAndParietalPleuralInvasion");
    }

    protected String _naaccrVersion;

    protected String _majorNaaccrVersion;

    protected String _minorNaaccrVersion;

    protected String _recordType;

    protected int _naaccrLineLength;

    public NaaccrLayout(String naaccrVersion, String recordType, int naaccrLineLength, String layoutId, boolean loadFields, boolean useDeprecatedFieldNames) {
        super();

        if (!(naaccrVersion.matches("\\d\\d\\d")))
            throw new RuntimeException("Unexpected NAACCR version format for " + layoutId);

        _naaccrVersion = naaccrVersion;
        _majorNaaccrVersion = naaccrVersion.substring(0, 2);
        _minorNaaccrVersion = naaccrVersion.substring(2);
        _recordType = recordType;
        _naaccrLineLength = naaccrLineLength;

        String fullRecType;
        if ("A".equals(getRecordType()))
            fullRecType = "Abstract";
        else if ("M".equals(getRecordType()))
            fullRecType = "Modified";
        else if ("C".equals(getRecordType()))
            fullRecType = "Confidential";
        else if ("I".equals(getRecordType()))
            fullRecType = "Incidence";
        else
            throw new RuntimeException("Unsupported rec type: " + getRecordType());

        try {
            FixedColumnLayoutXmlDto xmlLayout = new FixedColumnLayoutXmlDto();
            xmlLayout.setId(layoutId);
            xmlLayout.setName(LayoutFactory.getAvailableInternalLayouts().get(layoutId));
            xmlLayout.setVersion(naaccrVersion);
            xmlLayout.setDescription("Latest version of NAACCR " + _majorNaaccrVersion + " " + fullRecType + " (" + _majorNaaccrVersion + "." + _minorNaaccrVersion + ")");
            xmlLayout.setLength(getNaaccrLineLength());

            // I needed this optimization because NAACCR layouts have tons of fields, and we can't afford loading them each time we need to identify a file...
            if (loadFields) {
                String xmlFilename = "naaccr-" + getMajorNaaccrVersion() + "-layout.xml";
                FixedColumnLayoutXmlDto tmpXmlLayout = LayoutUtils.readFixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResourceAsStream("layout/fixed/naaccr/" + xmlFilename));

                // do not add the fields pass the required line length
                List<FixedColumnLayoutFieldXmlDto> fields = new ArrayList<>();
                for (FixedColumnLayoutFieldXmlDto f : tmpXmlLayout.getField())
                    if (f.getStart() <= getNaaccrLineLength() || f.getStart() > tmpXmlLayout.getLength()) // allow fields greater than the max length will ensure errors are properly reported...
                        fields.add(f);
                xmlLayout.setField(fields);

                // set default value for the record type
                FixedColumnLayoutFieldXmlDto field = xmlLayout.getField().get(0);
                if (!"recordType".equals(field.getName()))
                    throw new RuntimeException("Record Type should be the first field of a NAACCR layout!");
                field.setDefaultValue(getRecordType());
            }

            init(xmlLayout, useDeprecatedFieldNames);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to instantiate NAACCR layout", e);
        }
    }

    @Override
    protected String getDeprecatedFieldName(String name) {
        return _XML_TO_LAYOUT_MAPPING.getOrDefault(name, name);
    }

    public String getNaaccrVersion() {
        return _naaccrVersion;
    }

    protected String getMajorNaaccrVersion() {
        return _majorNaaccrVersion;
    }

    public String getRecordType() {
        return _recordType;
    }

    protected int getNaaccrLineLength() {
        return _naaccrLineLength;
    }

    public String getFieldDocByNaaccrItemNumber(Integer num) {
        Field field = getFieldByNaaccrItemNumber(num); // getting the field will ensure that we try to use the name for a legit (non-retired) field first
        return getFieldDocByNameOrNumber(field != null ? field.getName() : null, num);
    }

    @Override
    public String getFieldDocByName(String name) {
        return getFieldDocByNameOrNumber(name, null);
    }

    protected String getFieldDocByNameOrNumber(String name, Integer number) {
        FixedColumnsField field = getFieldByName(name);

        // documentation was added for retired fields starting with NAACCR 18; I used the NAACCR number for those since they isn't a corresponding field
        //String filename = field != null ? field.getName() : number != null ? number.toString() : null;
        String filename;
        if (field != null) {
            filename = field.getName();
            // I could make this lookup faster by persisting the invert map, but I am not sure it's really necessary...
            for (Entry<String, String> entry : _XML_TO_LAYOUT_MAPPING.entrySet()) {
                if (filename.equals(entry.getValue())) {
                    filename = entry.getKey();
                    break;
                }
            }
        }
        else
            filename = number != null ? number.toString() : null;
        if (filename == null)
            return null;

        // NAACCR started to provide the documentation for reserved fields in version 18...
        boolean reservedField = field != null && field.getName().startsWith("reserved") && Integer.parseInt(_majorNaaccrVersion) < 18;

        URL docPath;
        if (reservedField)
            docPath = Thread.currentThread().getContextClassLoader().getResource("layout/fixed/naaccr/doc/reserved.html");
        else
            docPath = Thread.currentThread().getContextClassLoader().getResource("layout/fixed/naaccr/doc/" + getDocFolder() + "/" + filename + ".html");
        if (docPath == null)
            return null;

        String result = null;
        try (Reader reader = new InputStreamReader(docPath.openStream(), StandardCharsets.UTF_8); Writer writer = new StringWriter()) {
            IOUtils.copy(reader, writer);
            result = writer.toString();
        }
        catch (IOException e) {
            /* do nothing, result will be null, as per specs */
        }

        if (reservedField && result != null)
            result = result.replace("[:ITEM_NUM:]", field.getNaaccrItemNum().toString()).replace("[:COLUMNS:]", field.getStart() + " - " + field.getEnd());

        return result;
    }

    protected String getDocFolder() {
        return "naaccr" + getMajorNaaccrVersion();
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        // I know, the correct way to do this is to use the class hierarchy, but I don't want to repeat the CSS in all the NAACCR sub-classes...
        String docFolder = getDocFolder();

        StringBuilder buf = new StringBuilder();
        buf.append(_CSS_STYLE_SUMMARY_TABLE);
        if ("naaccr12".equals(docFolder) || "naaccr13".equals(docFolder))
            buf.append(_CSS_STYLE_13_AND_PRIOR);
        else
            buf.append(_DEFAULT_CSS_14_AND_LATER);

        return buf.toString();
    }

    @Override
    public String validateLine(String line, Integer lineNumber) {
        StringBuilder msg = new StringBuilder();

        if (line == null || line.isEmpty()) {
            msg.append("line ").append(lineNumber).append(": line is empty");
        }
        else {
            String naaccrVersion = extractNaaccrVersion(line);
            String recordType = extractRecordType(line);

            if (naaccrVersion.isEmpty() || recordType.isEmpty()) {
                String ver = naaccrVersion.isEmpty() ? "blank" : ("'" + naaccrVersion + "'");
                String type = recordType.isEmpty() ? "blank" : ("'" + recordType + "'");
                msg.append("line ").append(lineNumber).append(": unable to determine data format; NAACCR Record Version (Item #50) is ").append(ver).append(", Record Type (Item #10) is ");
                msg.append(type).append(" and line length is ").append(LayoutUtils.formatNumber(line.length()));
            }
            else if (naaccrVersion.startsWith(getMajorNaaccrVersion())) {
                if (getRecordType().equals(recordType)) {
                    if (line.length() != getNaaccrLineLength()) {
                        String real = LayoutUtils.formatNumber(line.length());
                        msg.append("line ").append(lineNumber).append(": conflict between NAACCR Record Version (Item #50), Record Type (Item #10) and line length.\n        Rec Version=");
                        msg.append(naaccrVersion).append(". Rec Type=").append(recordType).append(". Expected length ").append(getNaaccrLineLength()).append(" but length in file is ").append(real);
                    }
                }
                else
                    msg.append("line ").append(lineNumber).append(": invalid Record Type (Item #10): '").append(recordType).append("' ; value should be '").append(getRecordType()).append("'.");
            }
            else
                msg.append("line ").append(lineNumber).append(": invalid NAACCR Record Version (Item #50): '").append(naaccrVersion).append("' ; value should start with '").append(
                        getMajorNaaccrVersion()).append("'.");
        }

        return msg.length() == 0 ? null : msg.toString();
    }

    @Override
    protected String cleanValue(String value, Field field) {
        if ("naaccrRecordVersion".equals(field.getName()))
            return getLayoutVersion();
        if ("recordType".equals(field.getName()))
            return getRecordType();
        return value;
    }

    protected String extractNaaccrVersion(String line) {
        return line.length() <= 19 ? "" : line.substring(16, 19).trim();
    }

    protected String extractRecordType(String line) {
        return line.isEmpty() ? "" : line.substring(0, 1).trim();
    }

    @Override
    public LayoutInfo buildFileInfo(String firstRecord, LayoutInfoDiscoveryOptions options) {
        if (firstRecord == null || firstRecord.isEmpty())
            return null;
        if (options == null)
            options = new LayoutInfoDiscoveryOptions();

        String naaccrVersion = extractNaaccrVersion(firstRecord);
        String recordType = extractRecordType(firstRecord);

        boolean sameNaaccrVersion = naaccrVersion.startsWith(getMajorNaaccrVersion()); // the library never supported to the "minor" versions of the layouts
        boolean sameRecordType = getRecordType().equals(recordType);
        boolean sameLineLength = firstRecord.length() == getNaaccrLineLength();

        LayoutInfo result = new LayoutInfo();
        result.setLayoutId(getLayoutId());
        result.setLayoutName(getLayoutName());
        result.setLineLength(getLayoutLineLength());

        if (sameNaaccrVersion && sameRecordType) {
            if (!sameLineLength)
                result.setErrorMessage("was expecting line length of " + getLayoutLineLength() + " but first line is " + firstRecord.length());
            return result;
        }

        if (sameLineLength && options.isFixedColumnAllowDiscoveryFromLineLength()) {
            boolean naaccrVersionOk = sameNaaccrVersion || (naaccrVersion.isEmpty() && options.isNaaccrAllowBlankVersion());
            boolean recordTypeOk = sameRecordType || (recordType.isEmpty() && options.isNaaccrAllowBlankRecordType());
            if (naaccrVersionOk && recordTypeOk)
                return result;
        }

        return null;
    }
}
