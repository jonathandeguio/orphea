UPDATE platform_config
SET default_branch = 'master'
WHERE default_branch IS NULL;