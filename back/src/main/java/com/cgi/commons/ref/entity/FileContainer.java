package com.cgi.commons.ref.entity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * File object.
 */
public class FileContainer implements Serializable {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Logger. */
	private static final transient Logger LOGGER = Logger.getLogger(FileContainer.class);

	/** Universal Unique Identifier (if the file is stored into the temporary directory). */
	private String uuid;

	/** File name. */
	private String name;

	/** File content. */
	private byte[] content;

	/** Is the content null ? (not link to content size to avoid loading content if not needed) */
	private boolean isNull = false;

	/**
	 * Returns the name of the file.
	 * @return the name of the file.
	 */
	public String getName() {
		return (name != null) ? name : "untitled";
	}

	/**
	 * Returns the real name of the file.
	 * @return the real name of the file.
	 */
	public String realName() {
		return name;
	}

	/**
	 * Setter for the name of the file.
	 * @param name the name of the file.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the content of the file.
	 * @return the content of the file.
	 */
	@JsonIgnore
	public byte[] getContent() {
		return content;
	}

	/**
	 * Setter for the content of the file.
	 * @param data the content of the file.
	 */
	@JsonIgnore
	public void setContent(byte[] data) {
		if (data == null) {
			this.content = null;
			this.isNull = true;
		} else {
			this.content = Arrays.copyOf(data, data.length);
			this.isNull = false;
		}
	}

	/**
	 * Indicates if the content is null.
	 * @return <code>true</code> if the content is null.
	 */
	public boolean isNull() {
		return isNull;
	}

	/**
	 * Set if the content is null.
	 * @param isNull <code>true</code> if the content is null.
	 */
	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	/**
	 * @return The identifier of this file into the temporary directory.
	 */
	public String getUuid() {
        return uuid;
    }

    /**
     * Sets the identifier of this file into the temporary directory.
     * 
     * @param uuid
     *            Universal Unique Identifier to set.
     */
	public void setUuid(String uuid) {
        this.uuid = uuid;
    }

	/**
	 * Returns the content type of the file (from file extension).
	 * 
	 * @return Content type of the file or {@code application/octet-stream} if the file name is {@code null}.
	 */
	public String contentType() {

		if (null != name) {
			MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();
			return mimeTypes.getContentType(name);
		}
		return "application/octet-stream";
	}

	/**
	 * Create a temp file from a file content. 
	 * @param container The file content.
	 * @deprecated Use TmpFileManager instead.
	 */
	@Deprecated
	public static File createTempFile(FileContainer container) {
		String filename = FilenameUtils.getName(container.getName());

		File file = null;

		try {
			file = File.createTempFile(filename, null);
		} catch (IOException ioEx) {
			LOGGER.error("Error while writing file " + container.getName(), ioEx);
		}

		if (file != null && container.getContent() != null) {
			try {
				FileUtils.writeByteArrayToFile(file, container.getContent());
				return file;
			} catch (IOException ioEx) {
				LOGGER.error("Error while writing file " + container.getName(), ioEx);
			}
		}
		return null;
	}
	
	/**
	 * Empty current container.
	 */
	public void deleteFile() {
		setContent(null);
		setNull(true);
		setName(null);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(content);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FileContainer other = (FileContainer) obj;
		if (!Arrays.equals(content, other.content)) {
			return false;
		}
		if (isNull != other.isNull) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
