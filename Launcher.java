package com.srs.projectOb;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.DatatypeConverter;

import com.srs.projectOb.Utils.AppConstant;
import com.srs.projectOb.Utils.AppUtils;
import com.srs.projectOb.Utils.EncryptFile;
import com.srs.projectOb.pojo.ProjectPojo;
import com.srs.projectOb.pojo.UserPojo;

public class Launcher extends TimerTask {

	public Launcher() {
		/**
		 * This is for only first time ... When application Start As soon As
		 * User click on Add memo (Only First time) Then it should take its
		 * first Snap
		 **/
		UserPojo userPojo = AppUtils.getUserPojo(null);
		ProjectPojo projectPojo = AppUtils.getProjectPojo();
		String memo = projectPojo.memo;
		// memo = memo.equals("") ? "": memo.substring(0, AppConstant.MAX_CHAR)
		// ;
		// clientnumber_userid_projectid_date_time.png
		AppUtils.logger.warn("Memo for image is " + memo);
		AppUtils.logger.warn("Project is " + projectPojo.projectName);
		AppUtils.logger.warn("Keyboard Events count= " + AppConstant.keyBoardEventCount);
		AppUtils.logger.warn("Mouse Events count= " + AppConstant.mouseEventCount);
		AppUtils.logger.warn("Random Time is " + AppConstant.randomTime);
		AppUtils.logger.warn("MemoId for image is " + projectPojo.memoId);

		try {
			captureScreen(AppConstant.PATH + projectPojo.clientNumber + "_" + userPojo.userId + "_" + projectPojo.id
					+ "_" + new SimpleDateFormat(AppConstant.DATE_FORMAT).format(new Date(System.currentTimeMillis()))
					+ "_" + new SimpleDateFormat(AppConstant.TIME_FORMAT).format(new Date(System.currentTimeMillis()))
					+ "_" + AppConstant.MEMO_DELIMETER + "_" + userPojo.authKey + "_"
					+ (AppConstant.mouseEventCount + AppConstant.keyBoardEventCount) + "#" + AppConstant.randomTime
					+ "_" + projectPojo.memoId + ".jpg", DatatypeConverter.printBase64Binary(memo.getBytes()));
		} catch (Exception e) {
			AppUtils.logger.warn("Exception on first time Snap Capture is " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Taking Snapshots and save in specified fix directory
	 *
	 * @param fileName
	 * @throws Exception
	 */
	public void captureScreen(String fileName, String memo) throws Exception {
		File tempFile = null;
		OutputStream os = null;
		ImageWriter writer = null;
		ImageOutputStream ios = null;
		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle screenRectangle = new Rectangle(screenSize);
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(screenRectangle);
			if (image != null) {
				AppUtils.logger.warn("Capture Screen");
			}
			/////////////////////////////////// Compression
			/////////////////////////////////// Logic////////////////////////
			String tempFileName = AppConstant.PATH.concat(String.valueOf(System.currentTimeMillis()).concat(".jpg"));
			tempFile = new File(tempFileName);
			os = new FileOutputStream(tempFile);

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
			writer = (ImageWriter) writers.next();

			ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);

			ImageWriteParam param = writer.getDefaultWriteParam();

			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(0.7f);
			writer.write(null, new IIOImage(image, null, null), param);
			AppUtils.changeExifMetadata(tempFile, new File(fileName), memo);

			new EncryptFile().encrypt(fileName, fileName + AppConstant.ENCRYPTOR_DECRYPTOR);
			ios.close();
		} catch (Exception e) {
			System.out.println("Esception in captureScreen :: " + e.getMessage());
		} finally {
			os.close();
			writer.dispose();
			tempFile.delete();

		}

	}

	@Override
	public void run() {
		try {
			AppConstant.count++;
			AppUtils.logger
					.warn("Count==" + AppConstant.count + " Delay=" + AppConstant.randomTime + " time=" + new Date());
			if (AppConstant.count == AppConstant.randomTime) {
				UserPojo userPojo = AppUtils.getUserPojo(null);
				ProjectPojo projectPojo = AppUtils.getProjectPojo();
				String memo = projectPojo.memo;
				// memo = memo.equals("") ? "": memo.substring(0,
				// AppConstant.MAX_CHAR) ;
				// clientnumber_userid_projectid_date_time.png
				AppUtils.logger.warn("Memo for image is " + memo);
				AppUtils.logger.warn("Memo ID for image is " + projectPojo.memoId);
				AppUtils.logger.warn("Project is " + projectPojo.projectName);
				AppUtils.logger.warn("Keyboard Events count= " + AppConstant.keyBoardEventCount);
				AppUtils.logger.warn("Mouse Events count= " + AppConstant.mouseEventCount);
				AppUtils.logger.warn("Random Time is " + AppConstant.randomTime);

				captureScreen(
						AppConstant.PATH + projectPojo.clientNumber + "_" + userPojo.userId + "_" + projectPojo.id + "_"
								+ new SimpleDateFormat(AppConstant.DATE_FORMAT)
										.format(new Date(System.currentTimeMillis()))
								+ "_"
								+ new SimpleDateFormat(AppConstant.TIME_FORMAT)
										.format(new Date(System.currentTimeMillis()))
								+ "_" + AppConstant.MEMO_DELIMETER + "_" + userPojo.authKey + "_"
								+ (AppConstant.mouseEventCount + AppConstant.keyBoardEventCount) + "#"
								+ AppConstant.randomTime + "_" + projectPojo.memoId + ".jpg",
						DatatypeConverter.printBase64Binary(memo.getBytes()));
				AppConstant.randomTime = (AppUtils.randInt(AppConstant.MIN_TIME, AppConstant.MAX_TIME));
				AppConstant.mouseEventCount = 0;
				AppConstant.keyBoardEventCount = 0;
				AppConstant.count = 0;
			}

		} catch (Exception ex) {
			AppUtils.logger.warn(ex);
		}

	}

}
