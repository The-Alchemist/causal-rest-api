/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.pitt.dbmi.ccd.causal.rest.api.dto.PriorKnowledgeFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.InternalErrorException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.NotFoundByIdException;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.UserNotFoundException;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class PriorKnowledgeFileEndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriorKnowledgeFileEndpointService.class);

    private final CausalRestProperties causalRestProperties;

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    @Autowired
    public PriorKnowledgeFileEndpointService(
            CausalRestProperties causalRestProperties,
            UserAccountService userAccountService,
            DataFileService dataFileService) {
        this.causalRestProperties = causalRestProperties;
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
    }

    /**
     * List all the available prior knowledge files for a given user ID
     *
     * @param uid
     * @return
     */
    public List<PriorKnowledgeFileDTO> listAllPriorKnowledgeFiles(Long uid) {
        List<PriorKnowledgeFileDTO> priorKnowledgeFileDTOs = new LinkedList<>();

        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        List<DataFile> dataFiles = dataFileService.findByUserAccount(userAccount);
        dataFiles.forEach(dataFile -> {
            String fileName = dataFile.getName();

            // Prior knowledge files must have .prior extension
            if (fileName.endsWith(".prior")) {
                PriorKnowledgeFileDTO priorKnowledgeFileDTO = new PriorKnowledgeFileDTO();

                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

                priorKnowledgeFileDTO.setId(dataFile.getId());
                priorKnowledgeFileDTO.setName(dataFile.getName());
                priorKnowledgeFileDTO.setCreationTime(dataFile.getCreationTime());
                priorKnowledgeFileDTO.setFileSize(dataFile.getFileSize());
                priorKnowledgeFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
                priorKnowledgeFileDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());

                priorKnowledgeFileDTOs.add(priorKnowledgeFileDTO);
            }
        });

        return priorKnowledgeFileDTOs;
    }

    /**
     * Delete a prior knowledge file for a given file ID of a given user
     *
     * @param id
     * @param uid
     */
    public void deleteByIdAndUid(Long id, Long uid) {
        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            throw new NotFoundByIdException(id);
        }

        try {
            // Delete records from data_file_info table and data_file table
            dataFileService.deleteDataFile(dataFile);
            // Delete the physical file from workspace folder
            Files.deleteIfExists(Paths.get(dataFile.getAbsolutePath(), dataFile.getName()));
            LOGGER.info(String.format("Prior knowledge file '%s' (id=%d) has been deleted.", dataFile.getName(), id));
        } catch (Exception exception) {
            String errMsg = String.format("Unable to delete Prior knowledge file id=%d.", id);
            LOGGER.error(errMsg, exception);
            throw new InternalErrorException(errMsg);
        }
    }

    /**
     * Get a prior knowledge file info for a given file ID of a given user
     *
     * @param id
     * @param uid
     * @return
     */
    public PriorKnowledgeFileDTO findByIdAndUid(Long id, Long uid) {
        UserAccount userAccount = userAccountService.findById(uid);
        if (userAccount == null) {
            throw new UserNotFoundException(uid);
        }

        DataFile dataFile = dataFileService.findByIdAndUserAccount(id, userAccount);
        if (dataFile == null) {
            LOGGER.warn(String.format("Can not find prior knowledge file id=%d for user id=%d.", id, uid));
            throw new NotFoundByIdException(id);
        }

        PriorKnowledgeFileDTO priorKnowledgeFileDTO = new PriorKnowledgeFileDTO();

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();

        priorKnowledgeFileDTO.setId(dataFile.getId());
        priorKnowledgeFileDTO.setName(dataFile.getName());
        priorKnowledgeFileDTO.setCreationTime(dataFile.getCreationTime());
        priorKnowledgeFileDTO.setFileSize(dataFile.getFileSize());
        priorKnowledgeFileDTO.setLastModifiedTime(dataFile.getLastModifiedTime());
        priorKnowledgeFileDTO.setMd5checkSum(dataFileInfo.getMd5checkSum());

        return priorKnowledgeFileDTO;
    }
}
