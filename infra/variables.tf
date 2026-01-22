variable "aws_region" {
  type        = string
  description = "AWS region for the RDS instance."
  default     = "ap-northeast-2"
}

variable "db_identifier" {
  type        = string
  description = "RDS instance identifier."
  default     = "job-radar-mysql"
}

variable "db_name" {
  type        = string
  description = "Initial database name."
  default     = "job"
}

variable "db_username" {
  type        = string
  description = "Master username for the DB."
  default     = "jobradar"
}

variable "db_port" {
  type        = number
  description = "MySQL port."
  default     = 3306
}

variable "allowed_cidrs" {
  type        = list(string)
  description = "CIDR blocks allowed to access MySQL. Set to your IP range for safety."
}

variable "allocated_storage_gb" {
  type        = number
  description = "Storage size in GB (free tier covers up to 20GB)."
  default     = 20
}

variable "instance_class" {
  type        = string
  description = "Instance class (free tier: db.t3.micro)."
  default     = "db.t3.micro"
}

variable "engine_version" {
  type        = string
  description = "MySQL engine version."
  default     = "8.0"
}
