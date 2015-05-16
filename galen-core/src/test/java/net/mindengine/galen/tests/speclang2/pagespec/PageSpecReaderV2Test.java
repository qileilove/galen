/*******************************************************************************
* Copyright 2015 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.galen.tests.speclang2.pagespec;

import net.mindengine.galen.browser.SeleniumBrowser;
import net.mindengine.galen.components.mocks.driver.MockedDriver;
import net.mindengine.galen.components.validation.MockedInvisiblePageElement;
import net.mindengine.galen.components.validation.MockedPage;
import net.mindengine.galen.components.validation.MockedPageElement;
import net.mindengine.galen.page.Page;
import net.mindengine.galen.page.PageElement;
import net.mindengine.galen.parser.FileSyntaxException;
import net.mindengine.galen.speclang2.reader.pagespec.PageSpecReaderV2;
import net.mindengine.galen.specs.page.CorrectionsRect;
import net.mindengine.galen.specs.page.Locator;
import net.mindengine.galen.specs.page.ObjectSpecs;
import net.mindengine.galen.specs.page.PageSection;
import net.mindengine.galen.specs.reader.page.PageSpec;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PageSpecReaderV2Test {

    private static final Page NO_PAGE = null;
    private static final List<String> EMPTY_TAGS = Collections.emptyList();
    private static final Properties NO_PROPERTIES = null;

    @Test
    public void shouldRead_objectDefinitions() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/object-definitions.gspec");


        assertThat(pageSpec.getObjects(), is((Map<String, Locator>)new HashMap<String, Locator>(){{
            put("header", new Locator("css", "#header"));
            put("header-icon", new Locator("css", "#header img"));
            put("button", new Locator("xpath", "//div[@id='button']"));
            put("cancel-link", new Locator("id", "cancel"));
            put("caption", new Locator("css", "#wrapper")
                    .withCorrections(new CorrectionsRect(
                            new CorrectionsRect.Correction(0, CorrectionsRect.Type.PLUS),
                            new CorrectionsRect.Correction(100, CorrectionsRect.Type.PLUS),
                            new CorrectionsRect.Correction(5, CorrectionsRect.Type.MINUS),
                            new CorrectionsRect.Correction(7, CorrectionsRect.Type.PLUS)
                    )));
        }}));
    }


    @Test
    public void shouldRead_objectDefinitions_withMultiObjects() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/object-definitions-multi-objects.gspec",
                new SeleniumBrowser(new MockedDriver("/speclang2/mocks/menu-items.json")).getPage(), asList("mobile"), EMPTY_TAGS);

        assertThat(pageSpec.getObjects(), is((Map<String, Locator>)new HashMap<String, Locator>(){{
            put("menu-item-1", new Locator("css", "#menu li", 1));
            put("menu-item-2", new Locator("css", "#menu li", 2));
            put("menu-item-3", new Locator("css", "#menu li", 3));
            put("menu-item-4", new Locator("css", "#menu li", 4));
        }}));
    }

    @Test
    public void shouldRead_objectDefinitions_withMultiLevelObjects() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/object-definitions-multi-level-objects.gspec",
                new SeleniumBrowser(new MockedDriver("/speclang2/mocks/multi-level-objects.json")).getPage(), asList("mobile"), EMPTY_TAGS);

        assertThat(pageSpec.getObjects(), is((Map<String, Locator>)new HashMap<String, Locator>(){{
            put("header", new Locator("css", "#header"));
            put("header.icon", new Locator("css", "img")
                    .withParent(new Locator("css", "#header")));

            put("box-1", new Locator("css", ".box", 1));
            put("box-1.caption", new Locator("css", ".caption")
                    .withParent(new Locator("css", ".box", 1)));

            put("box-2", new Locator("css", ".box", 2));
            put("box-2.caption", new Locator("css", ".caption")
                    .withParent(new Locator("css", ".box", 2)));

            put("box-3", new Locator("css", ".box", 3));
            put("box-3.caption", new Locator("css", ".caption")
                    .withParent(new Locator("css", ".box", 3)));
        }}));
    }


    @Test
    public void shouldRead_sectionsWithObjectSpecs() throws  IOException {
        PageSpec pageSpec = readPageSpec("speclang2/sections-with-object-specs.gspec");

        assertThat(pageSpec.getSections().size(), is(2));

        PageSection section1 = pageSpec.getSections().get(0);
        assertThat(section1.getObjects().size(), is(1));
        assertThat(section1.getObjects().get(0).getObjectName(), is("header"));
        assertThat(section1.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(section1.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("height 100px"));

        assertThat(section1.getSections().size(), is(1));
        PageSection subSection = section1.getSections().get(0);
        assertThat(subSection.getObjects().size(), is(2));
        assertThat(subSection.getObjects().get(0).getObjectName(), is("login-link"));
        assertThat(subSection.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("height 30px"));
        assertThat(subSection.getObjects().get(1).getObjectName(), is("register-link"));
        assertThat(subSection.getObjects().get(1).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(1).getSpecs().get(0).getOriginalText(), is("right-of login-link 10 to 30px"));

        PageSection section2 = pageSpec.getSections().get(1);
        assertThat(section2.getName(), is("Main section"));
        assertThat(section2.getObjects().size(), is(1));
        assertThat(section2.getObjects().get(0).getObjectName(), is("main-section"));
        assertThat(section2.getObjects().get(0).getSpecs().size(), is(2));
        assertThat(section2.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("below header 0 to 5px"));
        assertThat(section2.getObjects().get(0).getSpecs().get(1).getOriginalText(), is("inside screen 0px left right"));
    }

    @Test
    public void shouldProcess_complexObjectExpressions_insideSections() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/complex-object-expressions.gspec");

        PageSection section = pageSpec.getSections().get(0);

        assertThat(section.getName(), is("Main section"));
        assertThat(section.getObjects().size(), is(6));


        assertThat(section.getObjects().get(0).getObjectName(), is("header"));
        assertThat(section.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("inside screen 0px left right"));

        assertThat(section.getObjects().get(1).getObjectName(), is("menu"));
        assertThat(section.getObjects().get(1).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(1).getSpecs().get(0).getOriginalText(), is("inside screen 0px left right"));

        assertThat(section.getObjects().get(2).getObjectName(), is("main"));
        assertThat(section.getObjects().get(2).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(2).getSpecs().get(0).getOriginalText(), is("inside screen 0px left right"));

        assertThat(section.getObjects().get(3).getObjectName(), is("menu-item-1"));
        assertThat(section.getObjects().get(3).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(3).getSpecs().get(0).getOriginalText(), is("height 30px"));

        assertThat(section.getObjects().get(4).getObjectName(), is("menu-item-2"));
        assertThat(section.getObjects().get(4).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(4).getSpecs().get(0).getOriginalText(), is("height 30px"));

        assertThat(section.getObjects().get(5).getObjectName(), is("menu-item-3"));
        assertThat(section.getObjects().get(5).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(5).getSpecs().get(0).getOriginalText(), is("height 30px"));

    }

    @Test
    public void shouldProcess_hashInObjectPattern_asDigits() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/hash-in-object-name.gspec");

        PageSection section = pageSpec.getSections().get(0);
        assertThat(section.getObjects().size(), is(2));
        assertThat(section.getObjects().get(0).getObjectName(), is("item-1"));
        assertThat(section.getObjects().get(1).getObjectName(), is("item-342"));
    }



    /**
     * Purpose of this test is to check that "${}" expressions could be processed everywhere
     */
    @Test
    public void shouldRead_variablesDefinition_andProcessThem() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/variables-and-processing.gspec");


        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getName(), is("Section for user Johny"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("welcome-message"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("text is \"Welcome, Johny\""));

    }

    @Test
    public void shouldRead_simpleForLoop_andProcessIt() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/for-loop.gspec");

        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getName(), is("Main section"));
        assertThat(pageSpec.getSections().get(0).getObjects().size(), is(13));


        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();

        int objectIndex = 0;
        for (int i = 1; i <= 3; i++) {
            for (Integer j : asList(5, 7, 9)) {
                assertThat("Object #" + objectIndex + " name should be",
                        objects.get(objectIndex).getObjectName(),
                        is("box-" + i + "-" + j));

                assertThat("Object #" + objectIndex + " spec should be",
                        objects.get(objectIndex).getSpecs().get(0).getOriginalText(),
                        is("text is \"" + i + " and " + j + "\""));
                objectIndex++;
            }

            assertThat("Object #" + objectIndex + " name should be",
                    objects.get(objectIndex).getObjectName(),
                    is("label-" + i));

            assertThat("Object #" + objectIndex + " spec should be",
                    objects.get(objectIndex).getSpecs().get(0).getOriginalText(),
                    is("height 10px"));

            objectIndex++;
        }

        assertThat("Object #11 name should be",
                objects.get(objectIndex).getObjectName(),
                is("caption"));

        assertThat("Object #11 spec should be",
                objects.get(objectIndex).getSpecs().get(0).getOriginalText(),
                is("width 50px"));
    }


    @Test
    public void shouldRead_eachLoop_andProcessIt() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/foreach-loop.gspec");

        assertThat(pageSpec.getSections().size(), is(3));

        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(4));
        for (int i = 0; i < 4; i++) {
            assertThat("Section 1. Object #" + i + " name should be", objects.get(i).getObjectName(),
                    is("menu-item-" + (i+1)));

            assertThat("Section 1. Object #" + i + " spec should be", objects.get(i).getSpecs().get(0).getOriginalText(),
                    is("width 100px"));
        }

        List<ObjectSpecs> objects2 = pageSpec.getSections().get(1).getObjects();
        assertThat(objects2.size(), is(3));
        for (int i = 0; i < 3; i++) {
            assertThat("Section 2. Object #" + i + " name should be", objects2.get(i).getObjectName(),
                    is("menu-item-" + (i+2)));

            assertThat("Section 2. Object #" + i + " spec should be", objects2.get(i).getSpecs().get(0).getOriginalText(),
                    is("right-of menu-item-" + (i+1) + " 10px"));
        }

        List<ObjectSpecs> objects3 = pageSpec.getSections().get(2).getObjects();
        assertThat(objects3.size(), is(3));
        for (int i = 0; i < 3; i++) {
            assertThat("Section 3. Object #" + i + " name should be", objects3.get(i).getObjectName(),
                    is("menu-item-" + (i+1)));

            assertThat("Section 3. Object #" + i + " spec should be", objects3.get(i).getSpecs().get(0).getOriginalText(),
                    is("left-of menu-item-" + (i + 2) + " 10px"));
        }
    }

    @Test
    public void shouldRead_taggedSections_andProcessOnlyThose_thatMatchGivenTags_1() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/tagged-sections.gspec", NO_PAGE, asList("mobile"), EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));

        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(2));
        assertThat(objects.get(0).getObjectName(), is("header"));
        assertThat(objects.get(0).getSpecs().get(0).getOriginalText(), is("height 100px"));
        assertThat(objects.get(1).getObjectName(), is("header-icon"));
        assertThat(objects.get(1).getSpecs().get(0).getOriginalText(), is("inside header 5px top left"));
    }

    @Test
    public void shouldRead_taggedSections_andProcessOnlyThose_thatMatchGivenTags_2() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/tagged-sections.gspec", NO_PAGE, asList("tablet"), EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));

        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(2));
        assertThat(objects.get(0).getObjectName(), is("header"));
        assertThat(objects.get(0).getSpecs().get(0).getOriginalText(), is("height 100px"));
        assertThat(objects.get(1).getObjectName(), is("header-icon"));
        assertThat(objects.get(1).getSpecs().get(0).getOriginalText(), is("inside header 5px top left"));
    }

    @Test
    public void shouldRead_taggedSections_andProcessOnlyThose_thatMatchGivenTags_3() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/tagged-sections.gspec", NO_PAGE, asList("desktop"), EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));

        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(2));
        assertThat(objects.get(0).getObjectName(), is("header"));
        assertThat(objects.get(0).getSpecs().get(0).getOriginalText(), is("height 200px"));
        assertThat(objects.get(1).getObjectName(), is("header-icon"));
        assertThat(objects.get(1).getSpecs().get(0).getOriginalText(), is("inside header 5px top left"));
    }

    @Test
    public void shouldRead_taggedSections_andExcludeTags() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/tagged-sections.gspec", NO_PAGE, asList("mobile", "desktop"), asList("tablet"));

        assertThat(pageSpec.getSections().size(), is(1));

        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(2));
        assertThat(objects.get(0).getObjectName(), is("header"));
        assertThat(objects.get(0).getSpecs().get(0).getOriginalText(), is("height 200px"));
        assertThat(objects.get(1).getObjectName(), is("header-icon"));
        assertThat(objects.get(1).getSpecs().get(0).getOriginalText(), is("inside header 5px top left"));
    }

    @Test
    public void should_importOtherPageSpecs_onlyOnce_andMergeSectionsAndObjects() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/import-other-pagespecs.gspec");

        assertThat(pageSpec.getObjects(), is((Map<String, Locator>) new HashMap<String, Locator>() {{
            put("header", new Locator("css", "#header"));
            put("main-container", new Locator("css", "#main"));
        }}));

        assertThat(pageSpec.getSections().size(), is(2));
        assertThat(pageSpec.getSections().get(0).getName(), is("Header section"));
        assertThat(pageSpec.getSections().get(0).getObjects().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("header"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().size(), is(2));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("inside screen 0px top left right"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(1).getOriginalText(), is("height 100px"));

        assertThat(pageSpec.getSections().get(1).getName(), is("Main section"));
        assertThat(pageSpec.getSections().get(1).getObjects().size(), is(1));
        assertThat(pageSpec.getSections().get(1).getObjects().get(0).getObjectName(), is("main-container"));
        assertThat(pageSpec.getSections().get(1).getObjects().get(0).getSpecs().size(), is(1));
        assertThat(pageSpec.getSections().get(1).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("below header 0px"));
    }

    @Test
    public void shouldExecute_customJavaScript_fromSeparateFile() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/script-importing.gspec");

        assertThat(pageSpec.getSections().size(), is(1));
        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(2));
        assertThat(objects.get(0).getObjectName(), is("caption"));
        assertThat(objects.get(0).getSpecs().get(0).getOriginalText(), is("text is \"Awesome website!\""));
        assertThat(objects.get(1).getObjectName(), is("caption-2"));
        assertThat(objects.get(1).getSpecs().get(0).getOriginalText(), is("text is \"Welcome, Johny\""));
    }

    @Test
    public void shouldExecute_inPageJavaScript() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/script-inpage.gspec");

        assertThat(pageSpec.getSections().size(), is(1));
        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(2));
        assertThat(objects.get(0).getObjectName(), is("caption"));
        assertThat(objects.get(0).getSpecs().get(0).getOriginalText(), is("text is \"Awesome website!\""));
        assertThat(objects.get(1).getObjectName(), is("caption-2"));
        assertThat(objects.get(1).getSpecs().get(0).getOriginalText(), is("text is \"Welcome, Johny\""));

    }


    @Test
    public void shouldRead_customSpecRules_andProcessThem() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/custom-rules.gspec");

        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getName(), is("Main section"));
        assertThat(pageSpec.getSections().get(0).getSections().size(), is(1));

        PageSection subSection = pageSpec.getSections().get(0).getSections().get(0);
        assertThat(subSection.getName(), is("menu-item-* should be aligned horizontally"));
        assertThat(subSection.getObjects().size(), is(2));

        assertThat(subSection.getObjects().get(0).getObjectName(), is("menu-item-2"));
        assertThat(subSection.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("aligned horizontally all menu-item-1"));

        assertThat(subSection.getObjects().get(1).getObjectName(), is("menu-item-3"));
        assertThat(subSection.getObjects().get(1).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(1).getSpecs().get(0).getOriginalText(), is("aligned horizontally all menu-item-2"));


        assertThat(pageSpec.getSections().get(0).getObjects().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("menu-item-1"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().size(), is(0));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().get(0).getSpecs().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().get(0).getName(), is("squared"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().get(0).getSpecs().get(0).getOriginalText(),
                is("width 100 % of menu-item-1/height"));
    }


    @Test
    public void shouldRead_customRulesFromJavaScript_andProcessThem() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/custom-js-rules.gspec",
                new MockedPage(new HashMap<String, PageElement>()),
                EMPTY_TAGS, EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getName(), is("Main section"));
        assertThat(pageSpec.getSections().get(0).getSections().size(), is(1));

        PageSection subSection = pageSpec.getSections().get(0).getSections().get(0);
        assertThat(subSection.getName(), is("menu-item-* should be aligned horizontally"));
        assertThat(subSection.getObjects().size(), is(2));

        assertThat(subSection.getObjects().get(0).getObjectName(), is("menu-item-2"));
        assertThat(subSection.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("aligned horizontally all menu-item-1"));

        assertThat(subSection.getObjects().get(1).getObjectName(), is("menu-item-3"));
        assertThat(subSection.getObjects().get(1).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(1).getSpecs().get(0).getOriginalText(), is("aligned horizontally all menu-item-2"));


        assertThat(pageSpec.getSections().get(0).getObjects().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("menu-item-1"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().size(), is(0));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().get(0).getSpecs().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().get(0).getName(), is("squared"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecGroups().get(0).getSpecs().get(0).getOriginalText(),
                is("width 100 % of menu-item-1/height"));
    }

    @Test
    public void shouldRead_conditionsWithMultipleElseBlocks()  throws  IOException {
        PageSpec pageSpec = readPageSpec("speclang2/conditions.gspec",
                new MockedPage(new HashMap<String, PageElement>(){{
                    put("header", element(0, 0, 100, 10));
                }}),
                EMPTY_TAGS, EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));

        PageSection section = pageSpec.getSections().get(0);
        assertThat(section.getObjects().size(), is(1));
        assertThat(section.getObjects().get(0).getObjectName(), is("header-icon"));
        assertThat(section.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("inside header 10px top left"));
    }

    @Test
    public void shouldRead_conditionsWithMultipleElseBlocks_2()  throws  IOException {
        PageSpec pageSpec = readPageSpec("speclang2/conditions.gspec",
                new MockedPage(new HashMap<String, PageElement>(){{
                    put("header", invisibleElement(0, 0, 100, 10));
                    put("header2", element(0, 0, 100, 10));
                }}),
                EMPTY_TAGS, EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));

        PageSection section = pageSpec.getSections().get(0);
        assertThat(section.getObjects().size(), is(1));
        assertThat(section.getObjects().get(0).getObjectName(), is("header2-icon"));
        assertThat(section.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("inside header2 5px top left"));
    }

    @Test
    public void shouldRead_conditionsWithMultipleElseBlocks_3()  throws  IOException {
        PageSpec pageSpec = readPageSpec("speclang2/conditions.gspec",
                new MockedPage(new HashMap<String, PageElement>(){{
                    put("header", invisibleElement(0, 0, 100, 10));
                    put("header2", invisibleElement(0, 0, 100, 10));
                }}),
                EMPTY_TAGS, EMPTY_TAGS);

        assertThat(pageSpec.getSections().size(), is(1));

        PageSection section = pageSpec.getSections().get(0);
        assertThat(section.getObjects().size(), is(1));
        assertThat(section.getObjects().get(0).getObjectName(), is("header3"));
        assertThat(section.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(section.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("visible"));
    }

    @Test
    public void shouldAllow_toPassProperties() throws IOException {
        Properties properties = new Properties();
        properties.put("custom.user.name", "John");
        PageSpec pageSpec = new PageSpecReaderV2().read("speclang2/properties.gspec", NO_PAGE, EMPTY_TAGS, EMPTY_TAGS, properties);

        assertThat(pageSpec.getSections().get(0).getName(), is("Main section for user John"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(),
                is("text is \"Welcome, John!\""));
    }


    @Test
    public void shouldThrow_fileSyntaxException_ifThereIsAnErrorInSpec() throws IOException {
        try {
            readPageSpec("speclang2/syntax-error.gspec");
            throw new RuntimeException("FileSyntaxException was not thrown from page spec reader");
        } catch (FileSyntaxException ex) {
            assertThat(ex.getLine(), is(9));
            assertThat(ex.getFilePath(), endsWith("speclang2/syntax-error.gspec"));
            assertThat(ex.getCause().getMessage(), is("Expecting \"px\", \"to\" or \"%\", got \"\""));
        }
    }

    @Test
    public void shouldAllow_mathExpressions_onIntegerValues_onForLoopIndexes() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/for-loop-math-expression.gspec");
        PageSection section = pageSpec.getSections().get(0);

        assertThat(section.getObjects().size(), is(4));
        assertThat(section.getObjects().get(0).getSpecs().get(0).getOriginalText(),
                is("right-of menu-item-3"));
        assertThat(section.getObjects().get(1).getSpecs().get(0).getOriginalText(),
                is("right-of menu-item-5"));
        assertThat(section.getObjects().get(2).getSpecs().get(0).getOriginalText(),
                is("right-of menu-item-7"));
        assertThat(section.getObjects().get(3).getSpecs().get(0).getOriginalText(),
                is("right-of menu-item-9"));
    }

    @Test
    public void shouldRead_specsWithWarningLevel() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/warning-level-in-specs.gspec");

        PageSection section = pageSpec.getSections().get(0);

        assertThat(section.getObjects().get(0).getSpecs().get(0).isOnlyWarn(), is(false));
        assertThat(section.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("width 100px"));
        assertThat(section.getObjects().get(0).getSpecs().get(1).isOnlyWarn(), is(true));
        assertThat(section.getObjects().get(0).getSpecs().get(1).getOriginalText(), is("height 100px"));
    }

    @Test
    public void shouldRead_specAliases_beforeActualSpecs() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/spec-notes.gspec");
        PageSection section = pageSpec.getSections().get(0);

        assertThat(section.getObjects().size(), is(1));
        ObjectSpecs object = section.getObjects().get(0);

        assertThat(object.getSpecs().size(), is(3));
        assertThat(object.getSpecs().get(0).getAlias(), is(nullValue()));
        assertThat(object.getSpecs().get(0).getOriginalText(), is("height 100px"));
        assertThat(object.getSpecs().get(1).getAlias(), is("should be visible"));
        assertThat(object.getSpecs().get(1).getOriginalText(), is("visible"));
        assertThat(object.getSpecs().get(2).getAlias(), is("should be on top"));
        assertThat(object.getSpecs().get(2).getOriginalText(), is("inside screen 0px top"));
    }


    private PageSpec readPageSpec(String resource) throws IOException {
        return readPageSpec(resource, NO_PAGE, EMPTY_TAGS, EMPTY_TAGS);
    }

    private PageSpec readPageSpec(String resource, Page page, List<String> tags, List<String> excludedTags) throws IOException {
        return new PageSpecReaderV2().read(resource, page, tags, excludedTags, NO_PROPERTIES);
    }

    private MockedPageElement element(int left, int top, int width, int height) {
        return new MockedPageElement(left, top, width, height);
    }

    protected PageElement invisibleElement(int left, int top, int width, int height) {
        return new MockedInvisiblePageElement(left, top, width, height);
    }

}
