/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO. If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.spring;

import static uk.ac.bbsrc.tgac.miso.core.util.LimsUtils.isStringBlankOrNull;

import java.beans.PropertyEditorSupport;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomMapEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

import com.eaglegenomics.simlims.core.Group;
import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;
import com.eaglegenomics.simlims.core.manager.SecurityManager;

import uk.ac.bbsrc.tgac.miso.core.data.BoxSize;
import uk.ac.bbsrc.tgac.miso.core.data.BoxUse;
import uk.ac.bbsrc.tgac.miso.core.data.Dilution;
import uk.ac.bbsrc.tgac.miso.core.data.Experiment;
import uk.ac.bbsrc.tgac.miso.core.data.Index;
import uk.ac.bbsrc.tgac.miso.core.data.Kit;
import uk.ac.bbsrc.tgac.miso.core.data.Library;
import uk.ac.bbsrc.tgac.miso.core.data.LibraryDesign;
import uk.ac.bbsrc.tgac.miso.core.data.LibraryDesignCode;
import uk.ac.bbsrc.tgac.miso.core.data.Partition;
import uk.ac.bbsrc.tgac.miso.core.data.Platform;
import uk.ac.bbsrc.tgac.miso.core.data.Pool;
import uk.ac.bbsrc.tgac.miso.core.data.Project;
import uk.ac.bbsrc.tgac.miso.core.data.ReferenceGenome;
import uk.ac.bbsrc.tgac.miso.core.data.Run;
import uk.ac.bbsrc.tgac.miso.core.data.Sample;
import uk.ac.bbsrc.tgac.miso.core.data.SequencerPartitionContainer;
import uk.ac.bbsrc.tgac.miso.core.data.SequencerReference;
import uk.ac.bbsrc.tgac.miso.core.data.SequencingParameters;
import uk.ac.bbsrc.tgac.miso.core.data.Status;
import uk.ac.bbsrc.tgac.miso.core.data.Study;
import uk.ac.bbsrc.tgac.miso.core.data.StudyType;
import uk.ac.bbsrc.tgac.miso.core.data.impl.LibraryDilution;
import uk.ac.bbsrc.tgac.miso.core.data.impl.kit.KitDescriptor;
import uk.ac.bbsrc.tgac.miso.core.data.type.LibrarySelectionType;
import uk.ac.bbsrc.tgac.miso.core.data.type.LibraryStrategyType;
import uk.ac.bbsrc.tgac.miso.core.data.type.LibraryType;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.manager.RequestManager;
import uk.ac.bbsrc.tgac.miso.core.service.IndexService;
import uk.ac.bbsrc.tgac.miso.core.store.BoxStore;
import uk.ac.bbsrc.tgac.miso.core.store.LibraryDesignCodeDao;
import uk.ac.bbsrc.tgac.miso.core.store.LibraryDesignDao;
import uk.ac.bbsrc.tgac.miso.persistence.SequencingParametersDao;
import uk.ac.bbsrc.tgac.miso.service.ExperimentService;
import uk.ac.bbsrc.tgac.miso.service.KitService;
import uk.ac.bbsrc.tgac.miso.service.LibraryDilutionService;
import uk.ac.bbsrc.tgac.miso.service.LibraryService;
import uk.ac.bbsrc.tgac.miso.service.ReferenceGenomeService;
import uk.ac.bbsrc.tgac.miso.service.StudyService;

/**
 * Class that binds all the MISO model datatypes to the Spring form path types
 */
public class LimsBindingInitializer extends org.springframework.web.bind.support.ConfigurableWebBindingInitializer
    implements WebBindingInitializer {
  protected static final Logger log = LoggerFactory.getLogger(LimsBindingInitializer.class);

  @Autowired
  private RequestManager requestManager;

  @Autowired
  private SecurityManager securityManager;

  @Autowired
  private LibraryDesignDao libraryDesignDao;

  @Autowired
  private LibraryDesignCodeDao libraryDesignCodeDao;

  @Autowired
  private BoxStore sqlBoxDAO;

  @Autowired
  private LibraryDilutionService dilutionService;
  @Autowired
  private ExperimentService experimentService;
  @Autowired
  private IndexService indexService;
  @Autowired
  private StudyService studyService;
  @Autowired
  private LibraryService libraryService;
  @Autowired
  private ReferenceGenomeService referenceGenomeService;
  @Autowired
  private KitService kitService;

  @Autowired
  private SequencingParametersDao sequencingParametersDao;

  public void setKitService(KitService kitService) {
    this.kitService = kitService;
  }

  /**
   * Simplified interface to convert form data to fields.
   * 
   * @param <T>
   *          The target type of the conversion.
   */
  public static abstract class BindingConverter<T> {
    private final PropertyEditorSupport editor = new PropertyEditorSupport() {
      @Override
      public void setAsText(String element) throws IllegalArgumentException {
        try {
          setValue(resolve(element));
        } catch (Exception e) {
          throw new IllegalArgumentException("Failed to convert: " + element, e);
        }
      }
    };

    /**
     * Translate the string provided by the front end into a real object.
     */
    public abstract T resolve(String element) throws Exception;

    private final Class<T> clazz;

    public BindingConverter(Class<T> clazz) {
      this.clazz = clazz;
    }

    /**
     * Register this conversion with a Spring binder.
     * 
     * @param binder
     *          The binder to receive the registration
     * @param fields
     *          The field names allowed. I'm not sure why this is needed.
     * @return
     */
    public BindingConverter<T> register(WebDataBinder binder, String... fields) {
      binder.registerCustomEditor(clazz, editor);
      for (String field : fields) {
        binder.registerCustomEditor(clazz, field, editor);
      }
      return this;
    }

    /**
     * Register this conversion for a collection with a Spring binder.
     * 
     * @param binder
     *          The binder to receive the registration
     * @param collection
     *          The target type of the collection (i.e., the type of the field)
     * @param field
     *          The name of the field
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <C extends Collection> BindingConverter<T> register(WebDataBinder binder, Class<C> collection, String field) {
      binder.registerCustomEditor(collection, field, new CustomCollectionEditor(collection) {
        @Override
        protected Object convertElement(Object element) throws IllegalArgumentException {
          if (element instanceof String) {
            try {
              return resolve((String) element);
            } catch (Exception e) {
              throw new IllegalArgumentException("Failed to convert in collection.", e);
            }
          }
          return null;
        }
      });
      return this;
    }

    /**
     * Register this conversion for a map with a Spring binder.
     * 
     * @param binder
     *          The binder to receive the registration
     * @param collection
     *          The target type of the map (i.e., the type of the field)
     * @param field
     *          The name of the field
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <C extends Map> BindingConverter<T> registerMap(WebDataBinder binder, Class<C> collection, String field) {
      binder.registerCustomEditor(collection, field, new CustomMapEditor(collection) {

        @Override
        protected Object convertValue(Object element) {
          if (element instanceof String) {
            try {
              return resolve((String) element);
            } catch (Exception e) {
              throw new IllegalArgumentException("Failed to convert in map.", e);
            }
          }
          return null;
        }

      });
      return this;
    }
  }

  /**
   * Translate numeric form data to an object.
   */
  public static abstract class BindingConverterById<T> extends BindingConverter<T> {
    public BindingConverterById(Class<T> clazz) {
      super(clazz);
    }

    public abstract T resolveById(long id) throws Exception;

    @Override
    public T resolve(String element) throws Exception {
      if (isStringBlankOrNull(element)) {
        return null;
      }
      long id = Long.parseLong(element);
      return resolveById(id);
    }
  }

  /**
   * Translate a MISO-style identifier (with a specific prefix) into an object
   */
  public static abstract class BindingConverterByPrefixedId<T> extends BindingConverter<T> {
    private final String prefix;

    public BindingConverterByPrefixedId(Class<T> clazz, String prefix) {
      super(clazz);
      this.prefix = prefix;
    }

    public abstract T resolveById(long id) throws Exception;

    @Override
    public T resolve(String element) throws Exception {
      String numericPart = element.startsWith(prefix) ? element.substring(prefix.length()) : element;
      long id = Long.parseLong(numericPart);
      return resolveById(id);
    }
  }


  /**
   * Sets the requestManager of this LimsBindingInitializer object.
   * 
   * @param requestManager
   *          requestManager.
   */
  public void setRequestManager(RequestManager requestManager) {
    assert (requestManager != null);
    this.requestManager = requestManager;
  }

  /**
   * Sets the securityManager of this LimsBindingInitializer object.
   * 
   * @param securityManager
   *          securityManager.
   */
  public void setSecurityManager(SecurityManager securityManager) {
    assert (securityManager != null);
    this.securityManager = securityManager;
  }

  /**
   * Init this binder, registering all the custom editors to class types
   * 
   * @param binder
   *          of type WebDataBinder
   * @param req
   *          of type WebRequest
   */
  @Override
  public void initBinder(WebDataBinder binder, WebRequest req) {
    binder.setAutoGrowNestedPaths(false);

    binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, false));

    binder.registerCustomEditor(Boolean.class,
        new CustomBooleanEditor(CustomBooleanEditor.VALUE_TRUE, CustomBooleanEditor.VALUE_FALSE, true));

    binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));

    new BindingConverter<InetAddress>(InetAddress.class) {
      @Override
      public InetAddress resolve(String element) throws Exception {
        return InetAddress.getByName(element);
      }
    }.register(binder);

    new BindingConverterById<Group>(Group.class) {
      @Override
      public Group resolveById(long id) throws Exception {
        return securityManager.getGroupById(id);
      }
    }.register(binder).register(binder, Set.class, "groups");

    new BindingConverterById<User>(User.class) {
      @Override
      public User resolveById(long id) throws Exception {
        return securityManager.getUserById(id);
      }
    }.register(binder, "securityProfile.owner").register(binder, Set.class, "users");

    new BindingConverterByPrefixedId<Project>(Project.class, Project.PREFIX) {
      @Override
      public Project resolveById(long id) throws Exception {
        return requestManager.getProjectById(id);
      }
    }.register(binder);

    new BindingConverterByPrefixedId<Study>(Study.class, Study.PREFIX) {
      @Override
      public Study resolveById(long id) throws Exception {
        return studyService.get(id);
      }
    }.register(binder, "study").register(binder, Set.class, "studies");

    new BindingConverterById<Experiment>(Experiment.class) {
      @Override
      public Experiment resolveById(long id) throws Exception {
        return experimentService.get(id);
      }
    }.register(binder).register(binder, Set.class, "experiments");

    new BindingConverterById<Sample>(Sample.class) {
      @Override
      public Sample resolveById(long id) throws Exception {
        return requestManager.getSampleById(id);
      }
    }.register(binder, "sample").register(binder, Set.class, "samples");

    new BindingConverterById<Run>(Run.class) {
      @Override
      public Run resolveById(long id) throws Exception {
        return requestManager.getRunById(id);
      }

    }.register(binder).register(binder, Set.class, "runs");

    new BindingConverterById<Pool>(Pool.class) {
      @Override
      public Pool resolveById(long id) throws Exception {
        return requestManager.getPoolById(id);
      }
    }.register(binder, "sequencerPartitionContainers.partitions.pool");

    new BindingConverter<PlatformType>(PlatformType.class) {

      @Override
      public PlatformType resolve(String element) throws Exception {
        return PlatformType.get(element);
      }
    }.register(binder).register(binder, Set.class, "platformTypes");

    new BindingConverterById<StudyType>(StudyType.class) {

      @Override
      public StudyType resolveById(long id) throws Exception {
        return studyService.getType(id);
      }
    }.register(binder);

    new BindingConverterById<Partition>(Partition.class) {

      @Override
      public Partition resolveById(long id) throws Exception {
        return requestManager.getPartitionById(id);
      }

    }.register(binder, List.class, "partitions");

    new BindingConverterById<SequencerPartitionContainer>(SequencerPartitionContainer.class) {
      @Override
      public SequencerPartitionContainer resolveById(long id) throws Exception {
        return requestManager.getSequencerPartitionContainerById(id);
      }
    }.register(binder).register(binder, List.class, "containers");

    new BindingConverterById<Library>(Library.class) {
      @Override
      public Library resolveById(long id) throws Exception {
        return libraryService.get(id);
      }

    }.register(binder).register(binder, Set.class, "libraries");

    new BindingConverterByPrefixedId<Dilution>(Dilution.class, "LDI") {

      @Override
      public Dilution resolveById(long id) throws Exception {
        return dilutionService.get(id);
      }
    }.register(binder).register(binder,
        Set.class, "dilutions").register(binder, Set.class,
            "poolableElements");

    new BindingConverterById<LibraryDilution>(LibraryDilution.class) {
      @Override
      public LibraryDilution resolveById(long id) throws Exception {
        return dilutionService.get(id);
      }
    }.register(binder).register(binder, Set.class, "libraryDilutions");

    new BindingConverterById<LibraryType>(LibraryType.class) {
      @Override
      public LibraryType resolveById(long id) throws Exception {
        return libraryService.getLibraryTypeById(id);
      }

    }.register(binder).register(binder, Set.class, "libraryTypes");

    new BindingConverterById<LibrarySelectionType>(LibrarySelectionType.class) {
      @Override
      public LibrarySelectionType resolveById(long id) throws Exception {
        return libraryService.getLibrarySelectionTypeById(id);
      }
    }.register(binder).register(binder, Set.class, "librarySelectionTypes");

    new BindingConverterById<LibraryStrategyType>(LibraryStrategyType.class) {
      @Override
      public LibraryStrategyType resolveById(long id) throws Exception {
        return libraryService.getLibraryStrategyTypeById(id);
      }
    }.register(binder).register(binder, Set.class, "libraryStrategyTypes");

    new BindingConverterById<Index>(Index.class) {
      @Override
      public Index resolveById(long id) throws Exception {
        return indexService.getIndexById(id);
      }

    }.register(binder).registerMap(binder, HashMap.class, "indices");

    new BindingConverterById<SequencingParameters>(SequencingParameters.class) {
      @Override
      public SequencingParameters resolveById(long id) throws Exception {
        return sequencingParametersDao.getSequencingParameters(id);
      }

    }.register(binder);

    new BindingConverterById<Platform>(Platform.class) {
      @Override
      public Platform resolveById(long id) throws Exception {
        return requestManager.getPlatformById(id);
      }
    }.register(binder);

    new BindingConverterById<SequencerReference>(SequencerReference.class) {
      @Override
      public SequencerReference resolveById(long id) throws Exception {
        return requestManager.getSequencerReferenceById(id);
      }
    }.register(binder);

    new BindingConverterById<Status>(Status.class) {
      @Override
      public Status resolveById(long id) throws Exception {
        return requestManager.getStatusById(id);
      }
    }.register(binder);

    new BindingConverterById<SecurityProfile>(SecurityProfile.class) {
      @Override
      public SecurityProfile resolveById(long id) throws Exception {
        return securityManager.getSecurityProfileById(id);
      }

    }.register(binder);

    new BindingConverterById<Kit>(Kit.class) {
      @Override
      public Kit resolveById(long id) throws Exception {
        return kitService.getKitById(id);
      }
    }.register(binder).register(binder, Set.class, "kits");

    new BindingConverterById<KitDescriptor>(KitDescriptor.class) {
      @Override
      public KitDescriptor resolveById(long id) throws Exception {
        return kitService.getKitDescriptorById(id);
      }
    }.register(binder).register(binder, Set.class, "kitDescriptors");

    new BindingConverterById<ReferenceGenome>(ReferenceGenome.class) {

      @Override
      public ReferenceGenome resolveById(long id) throws Exception {
        return referenceGenomeService.get(id);
      }
    }.register(binder).register(binder, Set.class, "referenceGenomes");

    new BindingConverterById<LibraryDesign>(LibraryDesign.class) {
      @Override
      public LibraryDesign resolveById(long id) throws Exception {
        return id == -1 ? null : libraryDesignDao.getLibraryDesign(id);
      }

    }.register(binder);

    new BindingConverterById<LibraryDesignCode>(LibraryDesignCode.class) {

      @Override
      public LibraryDesignCode resolveById(long id) throws Exception {
        return id == -1 ? null : libraryDesignCodeDao.getLibraryDesignCode(id);
      }
    }.register(binder);

    new BindingConverterById<BoxUse>(BoxUse.class) {
      @Override
      public BoxUse resolveById(long id) throws Exception {
        return sqlBoxDAO.getUseById(id);
      }

    }.register(binder);
    new BindingConverterById<BoxSize>(BoxSize.class) {

      @Override
      public BoxSize resolveById(long id) throws Exception {
        return sqlBoxDAO.getSizeById(id);
      }

    }.register(binder);
  }

  public void setLibraryService(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  public void setDilutionService(LibraryDilutionService dilutionService) {
    this.dilutionService = dilutionService;
  }
}
