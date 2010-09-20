
namespace org.chieke.WinDroidNotifier
{
	partial class ExceptionForm
	{
		/// <summary>
		/// Designer variable used to keep track of non-visual components.
		/// </summary>
		private System.ComponentModel.IContainer components = null;
		
		/// <summary>
		/// Disposes resources used by the form.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing) {
				if (components != null) {
					components.Dispose();
				}
			}
			base.Dispose(disposing);
		}
		
		/// <summary>
		/// This method is required for Windows Forms designer support.
		/// Do not change the method contents inside the source code editor. The Forms designer might
		/// not be able to load this method if it was changed manually.
		/// </summary>
		private void InitializeComponent()
		{
			System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ExceptionForm));
			this.lblExceptionMessage = new System.Windows.Forms.Label();
			this.txtStackTrace = new System.Windows.Forms.RichTextBox();
			this.btnClose = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// lblExceptionMessage
			// 
			this.lblExceptionMessage.AutoSize = true;
			this.lblExceptionMessage.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.lblExceptionMessage.Location = new System.Drawing.Point(12, 9);
			this.lblExceptionMessage.Name = "lblExceptionMessage";
			this.lblExceptionMessage.Size = new System.Drawing.Size(123, 13);
			this.lblExceptionMessage.TabIndex = 0;
			this.lblExceptionMessage.Text = "[exception message]";
			// 
			// txtStackTrace
			// 
			this.txtStackTrace.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
									| System.Windows.Forms.AnchorStyles.Left) 
									| System.Windows.Forms.AnchorStyles.Right)));
			this.txtStackTrace.BackColor = System.Drawing.SystemColors.Window;
			this.txtStackTrace.Location = new System.Drawing.Point(12, 25);
			this.txtStackTrace.Name = "txtStackTrace";
			this.txtStackTrace.ReadOnly = true;
			this.txtStackTrace.Size = new System.Drawing.Size(608, 387);
			this.txtStackTrace.TabIndex = 1;
			this.txtStackTrace.Text = "";
			// 
			// btnClose
			// 
			this.btnClose.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.btnClose.Location = new System.Drawing.Point(545, 418);
			this.btnClose.Name = "btnClose";
			this.btnClose.Size = new System.Drawing.Size(75, 23);
			this.btnClose.TabIndex = 2;
			this.btnClose.Text = "Close";
			this.btnClose.UseVisualStyleBackColor = true;
			this.btnClose.Click += new System.EventHandler(this.BtnCloseClick);
			// 
			// ExceptionForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(632, 453);
			this.Controls.Add(this.btnClose);
			this.Controls.Add(this.txtStackTrace);
			this.Controls.Add(this.lblExceptionMessage);
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.MinimumSize = new System.Drawing.Size(640, 480);
			this.Name = "ExceptionForm";
			this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
			this.Text = "Exception";
			this.Load += new System.EventHandler(this.ExceptionFormLoad);
			this.ResumeLayout(false);
			this.PerformLayout();
		}
		private System.Windows.Forms.Label lblExceptionMessage;
		private System.Windows.Forms.RichTextBox txtStackTrace;
		private System.Windows.Forms.Button btnClose;
	}
}
